package com.accierta.settlement.infrastructure.persistence;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.settlement.domain.event.DebtGenerated;
import com.accierta.settlement.domain.event.DebtSettled;
import com.accierta.settlement.domain.event.PaymentRequested;
import com.accierta.settlement.domain.model.Debt;
import com.accierta.settlement.domain.model.DebtId;
import com.accierta.settlement.domain.model.DebtStatus;
import com.accierta.settlement.domain.port.out.DebtRepository;
import com.accierta.shared.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcDebtRepository implements DebtRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcDebtRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(Debt debt) {
        List<DomainEvent> newEvents = debt.pullUncommittedEvents();
        long initialVersion = debt.version() - newEvents.size();
        for (int index = 0; index < newEvents.size(); index++) {
            DomainEvent event = newEvents.get(index);
            jdbcTemplate.update(
                    """
                    insert into debt_event_store (aggregate_id, version, event_type, payload, occurred_at)
                    values (?, ?, ?, cast(? as jsonb), ?)
                    """,
                    debt.id().value(),
                    initialVersion + index + 1,
                    event.getClass().getSimpleName(),
                    serialize(event),
                    Timestamp.from(event.occurredAt()));
            if (event instanceof DebtGenerated generated) {
                jdbcTemplate.update(
                        """
                        insert into debt_projection (
                            debt_id, group_id, porra_id, from_member_id, to_member_id, amount, currency,
                            status, created_at, requested_at, settled_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        generated.debtId().value(),
                        generated.groupId().value(),
                        generated.porraId().value(),
                        generated.fromMemberId().value(),
                        generated.toMemberId().value(),
                        generated.amount(),
                        generated.currency(),
                        DebtStatus.PENDING.name(),
                        Timestamp.from(generated.occurredAt()),
                        null,
                        null);
            } else if (event instanceof PaymentRequested requested) {
                jdbcTemplate.update(
                        "update debt_projection set status = ?, requested_at = ? where debt_id = ?",
                        DebtStatus.PAYMENT_REQUESTED.name(),
                        Timestamp.from(requested.occurredAt()),
                        requested.debtId().value());
            } else if (event instanceof DebtSettled settled) {
                jdbcTemplate.update(
                        "update debt_projection set status = ?, settled_at = ? where debt_id = ?",
                        DebtStatus.PAID.name(),
                        Timestamp.from(settled.occurredAt()),
                        settled.debtId().value());
            }
        }
    }

    @Override
    public Optional<Debt> findById(DebtId debtId) {
        List<Debt> debts = jdbcTemplate.query(
                "select * from debt_projection where debt_id = ?",
                (rs, rowNum) -> Debt.reconstitute(
                        new DebtId(rs.getObject("debt_id", UUID.class)),
                        new GroupId(rs.getObject("group_id", UUID.class)),
                        new PorraId(rs.getObject("porra_id", UUID.class)),
                        new UserId(rs.getObject("from_member_id", UUID.class)),
                        new UserId(rs.getObject("to_member_id", UUID.class)),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        DebtStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("requested_at") == null ? null : rs.getTimestamp("requested_at").toInstant(),
                        rs.getTimestamp("settled_at") == null ? null : rs.getTimestamp("settled_at").toInstant(),
                        versionFor(debtId.value())),
                debtId.value());
        return debts.stream().findFirst();
    }

    private long versionFor(UUID debtId) {
        Long version = jdbcTemplate.queryForObject(
                "select coalesce(max(version), 0) from debt_event_store where aggregate_id = ?",
                Long.class,
                debtId);
        return version == null ? 0 : version;
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize debt event", exception);
        }
    }
}
