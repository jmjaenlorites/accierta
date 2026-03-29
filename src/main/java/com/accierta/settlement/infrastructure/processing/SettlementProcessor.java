package com.accierta.settlement.infrastructure.processing;

import com.accierta.betting.domain.model.PorraType;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.settlement.application.query.LeaderboardRankBy;
import com.accierta.settlement.application.query.SettlementQueryService;
import com.accierta.settlement.domain.model.Debt;
import com.accierta.settlement.domain.model.DebtId;
import com.accierta.settlement.domain.model.SettlementOutcome;
import com.accierta.settlement.domain.port.out.DebtRepository;
import com.accierta.settlement.domain.service.DebtGenerationCalculator;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SettlementProcessor {

    private final JdbcTemplate jdbcTemplate;
    private final DebtRepository debtRepository;
    private final DebtGenerationCalculator debtGenerationCalculator;
    private final SettlementQueryService settlementQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    public SettlementProcessor(
            JdbcTemplate jdbcTemplate,
            DebtRepository debtRepository,
            SettlementQueryService settlementQueryService,
            SimpMessagingTemplate messagingTemplate,
            Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.debtRepository = debtRepository;
        this.settlementQueryService = settlementQueryService;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
        this.debtGenerationCalculator = new DebtGenerationCalculator();
    }

    @Scheduled(fixedDelayString = "${accierta.settlement.fixed-delay-ms:10000}")
    public void process() {
        List<PorraProcessingRow> rows = jdbcTemplate.query(
                """
                select p.porra_id, p.group_id, p.title, p.type, p.tolerance, p.stake, p.status, g.currency
                from porra_projection p
                join groups_projection g on g.group_id = p.group_id
                where p.status in ('RESOLVED', 'CANCELLED')
                  and not exists (
                    select 1 from settlement_processed_porra_projection s where s.porra_id = p.porra_id
                  )
                order by p.resolution_date asc, p.created_at asc
                """,
                (rs, rowNum) -> new PorraProcessingRow(
                        rs.getObject("porra_id", UUID.class),
                        rs.getObject("group_id", UUID.class),
                        rs.getString("title"),
                        PorraType.valueOf(rs.getString("type")),
                        rs.getObject("tolerance", Double.class),
                        rs.getInt("stake"),
                        rs.getString("status"),
                        rs.getString("currency")));
        rows.forEach(this::processPorra);
    }

    @Transactional
    void processPorra(PorraProcessingRow row) {
        List<BetRow> bets = jdbcTemplate.query(
                """
                select member_id, prediction, placed_at
                from porra_bet_projection
                where porra_id = ?
                order by placed_at asc
                """,
                (rs, rowNum) -> new BetRow(
                        rs.getObject("member_id", UUID.class),
                        rs.getString("prediction"),
                        rs.getTimestamp("placed_at").toInstant()),
                row.porraId());
        Instant processedAt = Instant.now(clock);
        if ("CANCELLED".equals(row.status())) {
            for (BetRow bet : bets) {
                insertMemberOutcome(
                        row.groupId(),
                        row.porraId(),
                        bet.memberId(),
                        row.title(),
                        row.type(),
                        bet.prediction(),
                        "CANCELLED",
                        SettlementOutcome.CANCELLED,
                        BigDecimal.ZERO,
                        row.currency(),
                        processedAt);
            }
            markProcessed(row.porraId(), row.groupId(), "CANCELLED", processedAt);
            publishLeaderboard(row.groupId());
            return;
        }

        ResolutionRow resolution = jdbcTemplate.query(
                """
                select accepted_result, accepted_at
                from resolution_projection
                where porra_id = ?
                  and status = 'ACCEPTED'
                  and coalesce(proposal_type, 'RESOLUTION') = 'RESOLUTION'
                order by created_at desc
                limit 1
                """,
                (rs, rowNum) -> new ResolutionRow(
                        rs.getString("accepted_result"),
                        rs.getTimestamp("accepted_at").toInstant()),
                row.porraId()).stream().findFirst().orElse(null);
        if (resolution == null) {
            return;
        }

        var calculation = debtGenerationCalculator.calculate(
                row.type(),
                row.stake(),
                row.tolerance(),
                resolution.acceptedResult(),
                bets.stream().map(bet -> new DebtGenerationCalculator.BetRecord(bet.memberId(), bet.prediction())).toList());

        for (DebtGenerationCalculator.DebtInstruction debtInstruction : calculation.debts()) {
            Debt debt = Debt.generate(
                    DebtId.newId(),
                    new GroupId(row.groupId()),
                    new com.accierta.betting.domain.model.PorraId(row.porraId()),
                    new UserId(debtInstruction.fromMemberId()),
                    new UserId(debtInstruction.toMemberId()),
                    debtInstruction.amount(),
                    row.currency(),
                    processedAt);
            debtRepository.save(debt);
        }

        for (BetRow bet : bets) {
            DebtGenerationCalculator.MemberOutcomeRecord outcomeRecord = calculation.outcomes().stream()
                    .filter(outcome -> outcome.memberId().equals(bet.memberId()))
                    .findFirst()
                    .orElse(new DebtGenerationCalculator.MemberOutcomeRecord(
                            bet.memberId(),
                            SettlementOutcome.REFUNDED,
                            BigDecimal.ZERO));
            insertMemberOutcome(
                    row.groupId(),
                    row.porraId(),
                    bet.memberId(),
                    row.title(),
                    row.type(),
                    bet.prediction(),
                    resolution.acceptedResult(),
                    outcomeRecord.outcome(),
                    outcomeRecord.moneyChange(),
                    row.currency(),
                    resolution.acceptedAt());
        }
        markProcessed(row.porraId(), row.groupId(), "RESOLVED", processedAt);
        publishLeaderboard(row.groupId());
    }

    private void insertMemberOutcome(
            UUID groupId,
            UUID porraId,
            UUID memberId,
            String title,
            PorraType type,
            String myBet,
            String result,
            SettlementOutcome outcome,
            BigDecimal moneyChange,
            String currency,
            Instant resolvedAt) {
        jdbcTemplate.update(
                """
                insert into settlement_member_porra_projection (
                    group_id, porra_id, member_id, title, type, my_bet, result,
                    outcome, money_change, currency, resolved_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                groupId,
                porraId,
                memberId,
                title,
                type.name(),
                myBet,
                result,
                outcome.name(),
                moneyChange,
                currency,
                Timestamp.from(resolvedAt));
    }

    private void markProcessed(UUID porraId, UUID groupId, String outcome, Instant processedAt) {
        jdbcTemplate.update(
                """
                insert into settlement_processed_porra_projection (porra_id, group_id, outcome, processed_at)
                values (?, ?, ?, ?)
                """,
                porraId,
                groupId,
                outcome,
                Timestamp.from(processedAt));
    }

    private void publishLeaderboard(UUID groupId) {
        messagingTemplate.convertAndSend(
                "/group/" + groupId + "/leaderboard",
                settlementQueryService.getLeaderboard(groupId, LeaderboardRankBy.NET_MONEY));
    }

    private record PorraProcessingRow(
            UUID porraId,
            UUID groupId,
            String title,
            PorraType type,
            Double tolerance,
            int stake,
            String status,
            String currency) {
    }

    private record BetRow(
            UUID memberId,
            String prediction,
            Instant placedAt) {
    }

    private record ResolutionRow(
            String acceptedResult,
            Instant acceptedAt) {
    }
}
