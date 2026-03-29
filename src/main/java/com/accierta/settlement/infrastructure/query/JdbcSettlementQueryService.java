package com.accierta.settlement.infrastructure.query;

import com.accierta.settlement.application.handler.SettlementForbiddenException;
import com.accierta.settlement.application.query.LeaderboardRankBy;
import com.accierta.settlement.application.query.LeaderboardView;
import com.accierta.settlement.application.query.MemberHistoryPageView;
import com.accierta.settlement.application.query.MemberStatsView;
import com.accierta.settlement.application.query.PersonalDebtsView;
import com.accierta.settlement.application.query.SettlementQueryService;
import com.accierta.settlement.domain.model.DebtStatus;
import com.accierta.settlement.domain.model.SettlementOutcome;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcSettlementQueryService implements SettlementQueryService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSettlementQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PersonalDebtsView getMyDebts(UUID groupId, UUID memberId) {
        ensureKnownMember(groupId, memberId);
        List<PersonalDebtsView.OutgoingDebtView> iOwe = jdbcTemplate.query(
                """
                select d.debt_id, d.porra_id, p.title, u.user_id, u.email, d.amount, d.currency, d.status, d.created_at
                from debt_projection d
                join porra_projection p on p.porra_id = d.porra_id
                join identity_user_projection u on u.user_id = d.to_member_id
                where d.group_id = ? and d.from_member_id = ?
                order by d.created_at desc
                """,
                (rs, rowNum) -> new PersonalDebtsView.OutgoingDebtView(
                        rs.getObject("debt_id", UUID.class),
                        rs.getObject("porra_id", UUID.class),
                        rs.getString("title"),
                        new PersonalDebtsView.MemberView(
                                rs.getObject("user_id", UUID.class),
                                rs.getString("email")),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        DebtStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant()),
                groupId,
                memberId);
        List<PersonalDebtsView.IncomingDebtView> owedToMe = jdbcTemplate.query(
                """
                select d.debt_id, d.porra_id, p.title, u.user_id, u.email, d.amount, d.currency, d.status, d.created_at
                from debt_projection d
                join porra_projection p on p.porra_id = d.porra_id
                join identity_user_projection u on u.user_id = d.from_member_id
                where d.group_id = ? and d.to_member_id = ?
                order by d.created_at desc
                """,
                (rs, rowNum) -> new PersonalDebtsView.IncomingDebtView(
                        rs.getObject("debt_id", UUID.class),
                        rs.getObject("porra_id", UUID.class),
                        rs.getString("title"),
                        new PersonalDebtsView.MemberView(
                                rs.getObject("user_id", UUID.class),
                                rs.getString("email")),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        DebtStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant()),
                groupId,
                memberId);
        String currency = groupCurrency(groupId);
        Map<UUID, BigDecimal> netByMember = new HashMap<>();
        jdbcTemplate.query(
                """
                select from_member_id, to_member_id, amount
                from debt_projection
                where group_id = ? and status <> 'PAID'
                  and (from_member_id = ? or to_member_id = ?)
                """,
                rs -> {
                    UUID fromMember = rs.getObject("from_member_id", UUID.class);
                    UUID toMember = rs.getObject("to_member_id", UUID.class);
                    BigDecimal amount = rs.getBigDecimal("amount");
                    UUID otherMember = fromMember.equals(memberId) ? toMember : fromMember;
                    BigDecimal current = netByMember.getOrDefault(otherMember, BigDecimal.ZERO);
                    BigDecimal delta = fromMember.equals(memberId) ? amount.negate() : amount;
                    netByMember.put(otherMember, current.add(delta));
                },
                groupId,
                memberId,
                memberId);
        List<PersonalDebtsView.ConsolidatedDebtView> consolidated = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : netByMember.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            String email = jdbcTemplate.queryForObject(
                    "select email from identity_user_projection where user_id = ?",
                    String.class,
                    entry.getKey());
            consolidated.add(new PersonalDebtsView.ConsolidatedDebtView(
                    new PersonalDebtsView.MemberView(entry.getKey(), email),
                    entry.getValue().setScale(2, RoundingMode.HALF_UP),
                    currency,
                    entry.getValue().signum() < 0 ? "I_OWE" : "OWED_TO_ME"));
        }
        return new PersonalDebtsView(iOwe, owedToMe, consolidated);
    }

    @Override
    public LeaderboardView getLeaderboard(UUID groupId, LeaderboardRankBy rankBy) {
        List<MemberRecord> members = loadMembers(groupId);
        String currency = groupCurrency(groupId);
        List<LeaderboardView.RankingEntryView> rankings = buildRankings(groupId, members, currency, rankBy);
        return new LeaderboardView(rankBy, currency, rankings);
    }

    @Override
    public MemberStatsView getMemberStats(UUID groupId, UUID memberId) {
        ensureKnownMember(groupId, memberId);
        return buildStats(groupId, memberId, groupCurrency(groupId));
    }

    @Override
    public MemberHistoryPageView getMemberHistory(UUID groupId, UUID memberId, int page, int size) {
        ensureKnownMember(groupId, memberId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Integer total = jdbcTemplate.queryForObject(
                """
                select count(*)
                from settlement_member_porra_projection
                where group_id = ? and member_id = ?
                """,
                Integer.class,
                groupId,
                memberId);
        int totalRecords = total == null ? 0 : total;
        int totalPages = totalRecords == 0 ? 0 : (int) Math.ceil((double) totalRecords / safeSize);
        List<MemberHistoryPageView.HistoryEntryView> content = jdbcTemplate.query(
                """
                select porra_id, title, type, my_bet, result, outcome, money_change, currency, resolved_at
                from settlement_member_porra_projection
                where group_id = ? and member_id = ?
                order by resolved_at desc
                limit ? offset ?
                """,
                (rs, rowNum) -> new MemberHistoryPageView.HistoryEntryView(
                        rs.getObject("porra_id", UUID.class),
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getString("my_bet"),
                        rs.getString("result"),
                        rs.getString("outcome"),
                        rs.getBigDecimal("money_change"),
                        rs.getString("currency"),
                        rs.getTimestamp("resolved_at").toInstant()),
                groupId,
                memberId,
                safeSize,
                safePage * safeSize);
        return new MemberHistoryPageView(content, safePage, totalPages);
    }

    private List<LeaderboardView.RankingEntryView> buildRankings(
            UUID groupId,
            List<MemberRecord> members,
            String currency,
            LeaderboardRankBy rankBy) {
        List<MemberWithStats> computed = members.stream()
                .map(member -> new MemberWithStats(member, buildStats(groupId, member.userId(), currency)))
                .toList();
        List<MemberWithStats> filtered = computed.stream()
                .filter(member -> switch (rankBy) {
                    case WIN_RATIO, MONEY_RATIO -> member.stats().totalPorras() >= 3;
                    default -> true;
                })
                .sorted(comparatorFor(rankBy))
                .toList();
        List<LeaderboardView.RankingEntryView> rankings = new ArrayList<>();
        for (int index = 0; index < filtered.size(); index++) {
            MemberWithStats member = filtered.get(index);
            rankings.add(new LeaderboardView.RankingEntryView(
                    index + 1,
                    member.record().userId(),
                    member.record().email(),
                    member.record().left(),
                    member.stats()));
        }
        return rankings;
    }

    private Comparator<MemberWithStats> comparatorFor(LeaderboardRankBy rankBy) {
        return switch (rankBy) {
            case PORRAS_WON -> Comparator
                    .comparing((MemberWithStats member) -> member.stats().won()).reversed()
                    .thenComparing(member -> member.stats().winRatio(), Comparator.reverseOrder())
                    .thenComparing(member -> member.record().joinedAt());
            case WIN_RATIO -> Comparator
                    .comparing((MemberWithStats member) -> member.stats().winRatio(), Comparator.reverseOrder())
                    .thenComparing(member -> member.stats().won(), Comparator.reverseOrder())
                    .thenComparing(member -> member.record().joinedAt());
            case NET_MONEY -> Comparator
                    .comparing((MemberWithStats member) -> member.stats().netMoney(), Comparator.reverseOrder())
                    .thenComparing(member -> member.stats().moneyRatio(), Comparator.reverseOrder())
                    .thenComparing(member -> member.record().joinedAt());
            case MONEY_RATIO -> Comparator
                    .comparing((MemberWithStats member) -> member.stats().moneyRatio(), Comparator.reverseOrder())
                    .thenComparing(member -> member.stats().netMoney(), Comparator.reverseOrder())
                    .thenComparing(member -> member.record().joinedAt());
        };
    }

    private MemberStatsView buildStats(UUID groupId, UUID memberId, String currency) {
        List<OutcomeRow> outcomes = jdbcTemplate.query(
                """
                select outcome, money_change, resolved_at
                from settlement_member_porra_projection
                where group_id = ? and member_id = ?
                order by resolved_at desc
                """,
                (rs, rowNum) -> new OutcomeRow(
                        SettlementOutcome.valueOf(rs.getString("outcome")),
                        rs.getBigDecimal("money_change"),
                        rs.getTimestamp("resolved_at").toInstant()),
                groupId,
                memberId);
        int totalPorras = outcomes.size();
        int won = 0;
        int lost = 0;
        int cancelled = 0;
        BigDecimal totalMoneyWon = BigDecimal.ZERO;
        BigDecimal totalMoneyLost = BigDecimal.ZERO;
        int currentStreak = 0;
        boolean streakBroken = false;
        for (OutcomeRow outcome : outcomes) {
            switch (outcome.outcome()) {
                case WON -> {
                    won++;
                    totalMoneyWon = totalMoneyWon.add(outcome.moneyChange().max(BigDecimal.ZERO));
                    if (!streakBroken) {
                        currentStreak++;
                    }
                }
                case LOST -> {
                    lost++;
                    totalMoneyLost = totalMoneyLost.add(outcome.moneyChange().abs());
                    streakBroken = true;
                }
                case CANCELLED -> cancelled++;
                case REFUNDED -> {
                }
            }
        }
        BigDecimal denominator = BigDecimal.valueOf(won + lost);
        BigDecimal winRatio = denominator.signum() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(won).divide(denominator, 3, RoundingMode.HALF_UP);
        BigDecimal moneyDenominator = totalMoneyWon.add(totalMoneyLost);
        BigDecimal moneyRatio = moneyDenominator.signum() == 0
                ? BigDecimal.ZERO
                : totalMoneyWon.divide(moneyDenominator, 3, RoundingMode.HALF_UP);
        BigDecimal pendingOwed = sumPending(groupId, memberId, true);
        BigDecimal pendingOwing = sumPending(groupId, memberId, false);
        return new MemberStatsView(
                totalPorras,
                won,
                lost,
                cancelled,
                winRatio,
                currentStreak,
                totalMoneyWon.setScale(2, RoundingMode.HALF_UP),
                totalMoneyLost.setScale(2, RoundingMode.HALF_UP),
                totalMoneyWon.subtract(totalMoneyLost).setScale(2, RoundingMode.HALF_UP),
                moneyRatio,
                pendingOwed,
                pendingOwing,
                currency);
    }

    private BigDecimal sumPending(UUID groupId, UUID memberId, boolean owedToMember) {
        BigDecimal amount = jdbcTemplate.queryForObject(
                """
                select coalesce(sum(amount), 0)
                from debt_projection
                where group_id = ?
                  and status <> 'PAID'
                  and %s = ?
                """.formatted(owedToMember ? "to_member_id" : "from_member_id"),
                BigDecimal.class,
                groupId,
                memberId);
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private List<MemberRecord> loadMembers(UUID groupId) {
        return jdbcTemplate.query(
                """
                select gm.user_id, gm.joined_at, gm.left_at, iu.email
                from group_members_projection gm
                join identity_user_projection iu on iu.user_id = gm.user_id
                where gm.group_id = ?
                order by gm.joined_at asc
                """,
                (rs, rowNum) -> new MemberRecord(
                        rs.getObject("user_id", UUID.class),
                        rs.getString("email"),
                        rs.getTimestamp("joined_at").toInstant(),
                        rs.getTimestamp("left_at") != null),
                groupId);
    }

    private void ensureKnownMember(UUID groupId, UUID memberId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from group_members_projection where group_id = ? and user_id = ?",
                Integer.class,
                groupId,
                memberId);
        if (count == null || count == 0) {
            throw new SettlementForbiddenException("not a group member");
        }
    }

    private String groupCurrency(UUID groupId) {
        String currency = jdbcTemplate.queryForObject(
                "select currency from groups_projection where group_id = ?",
                String.class,
                groupId);
        if (currency == null) {
            throw new java.util.NoSuchElementException("group not found");
        }
        return currency;
    }

    private record MemberRecord(
            UUID userId,
            String email,
            Instant joinedAt,
            boolean left) {
    }

    private record MemberWithStats(
            MemberRecord record,
            MemberStatsView stats) {
    }

    private record OutcomeRow(
            SettlementOutcome outcome,
            BigDecimal moneyChange,
            Instant resolvedAt) {
    }
}
