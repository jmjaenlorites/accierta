package com.accierta.betting.infrastructure.persistence;

import com.accierta.betting.domain.event.BetModified;
import com.accierta.betting.domain.event.BetPlaced;
import com.accierta.betting.domain.event.BettingDeadlineReached;
import com.accierta.betting.domain.event.PorraCreated;
import com.accierta.betting.domain.event.PorraAutoCancelled;
import com.accierta.betting.domain.event.PorraDeletedByCreator;
import com.accierta.betting.domain.event.ResolutionEnabled;
import com.accierta.betting.domain.model.Bet;
import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.Porra;
import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.betting.domain.port.out.PorraRepository;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
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
public class JdbcPorraRepository implements PorraRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcPorraRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(Porra porra) {
        List<DomainEvent> newEvents = porra.pullUncommittedEvents();
        long initialVersion = porra.version() - newEvents.size();
        for (int index = 0; index < newEvents.size(); index++) {
            DomainEvent event = newEvents.get(index);
            jdbcTemplate.update(
                    """
                    insert into porra_event_store (aggregate_id, version, event_type, payload, occurred_at)
                    values (?, ?, ?, cast(? as jsonb), ?)
                    """,
                    porra.id().value(),
                    initialVersion + index + 1,
                    event.getClass().getSimpleName(),
                    serialize(event),
                    Timestamp.from(event.occurredAt()));

            if (event instanceof PorraCreated created) {
                jdbcTemplate.update(
                        """
                        insert into porra_projection (
                            porra_id, group_id, creator_id, title, description, type, tolerance, stake,
                            betting_deadline, resolution_date, visibility, bet_modifiable, status, created_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        created.porraId().value(),
                        created.groupId().value(),
                        created.creatorId().value(),
                        created.title(),
                        created.description(),
                        created.type().name(),
                        created.tolerance(),
                        created.stake(),
                        Timestamp.from(created.bettingDeadline()),
                        Timestamp.from(created.resolutionDate()),
                        created.visibility().name(),
                        created.betModifiable(),
                        PorraStatus.OPEN.name(),
                        Timestamp.from(created.occurredAt()));
                for (String option : created.options()) {
                    jdbcTemplate.update(
                            "insert into porra_option_projection (porra_id, option_value) values (?, ?)",
                            created.porraId().value(),
                            option);
                }
            } else if (event instanceof PorraDeletedByCreator deleted) {
                jdbcTemplate.update(
                        "update porra_projection set status = ? where porra_id = ?",
                        PorraStatus.CANCELLED.name(),
                        deleted.porraId().value());
            } else if (event instanceof BettingDeadlineReached deadlineReached) {
                jdbcTemplate.update(
                        "update porra_projection set status = ? where porra_id = ?",
                        PorraStatus.CLOSED.name(),
                        deadlineReached.porraId().value());
            } else if (event instanceof PorraAutoCancelled autoCancelled) {
                jdbcTemplate.update(
                        "update porra_projection set status = ? where porra_id = ?",
                        PorraStatus.CANCELLED.name(),
                        autoCancelled.porraId().value());
            } else if (event instanceof ResolutionEnabled resolutionEnabled) {
                jdbcTemplate.update(
                        "update porra_projection set status = ? where porra_id = ?",
                        PorraStatus.PENDING_RESOLUTION.name(),
                        resolutionEnabled.porraId().value());
            } else if (event instanceof BetPlaced placed) {
                jdbcTemplate.update(
                        """
                        insert into porra_bet_projection (porra_id, member_id, prediction, placed_at)
                        values (?, ?, ?, ?)
                        on conflict (porra_id, member_id) do update set
                            prediction = excluded.prediction,
                            placed_at = excluded.placed_at
                        """,
                        placed.porraId().value(),
                        placed.memberId().value(),
                        placed.prediction(),
                        Timestamp.from(placed.occurredAt()));
            } else if (event instanceof BetModified modified) {
                jdbcTemplate.update(
                        """
                        update porra_bet_projection
                        set prediction = ?, placed_at = ?
                        where porra_id = ? and member_id = ?
                        """,
                        modified.newPrediction(),
                        Timestamp.from(modified.occurredAt()),
                        modified.porraId().value(),
                        modified.memberId().value());
            }
        }
    }

    @Override
    public Optional<Porra> findById(PorraId porraId) {
        List<Porra> porras = jdbcTemplate.query(
                "select * from porra_projection where porra_id = ?",
                (rs, rowNum) -> {
                    List<String> options = jdbcTemplate.query(
                            "select option_value from porra_option_projection where porra_id = ? order by option_value",
                            (ors, idx) -> ors.getString("option_value"),
                            porraId.value());
                    List<Bet> bets = jdbcTemplate.query(
                            "select member_id, prediction, placed_at from porra_bet_projection where porra_id = ?",
                            (brs, idx) -> new Bet(
                                    new UserId(brs.getObject("member_id", UUID.class)),
                                    brs.getString("prediction"),
                                    brs.getTimestamp("placed_at").toInstant()),
                            porraId.value());
                    Long currentVersion = jdbcTemplate.queryForObject(
                            "select coalesce(max(version), 0) from porra_event_store where aggregate_id = ?",
                            Long.class,
                            porraId.value());
                    return Porra.reconstitute(
                            new PorraId(rs.getObject("porra_id", UUID.class)),
                            new GroupId(rs.getObject("group_id", UUID.class)),
                            new UserId(rs.getObject("creator_id", UUID.class)),
                            rs.getString("title"),
                            rs.getString("description"),
                            PorraType.valueOf(rs.getString("type")),
                            options,
                            rs.getObject("tolerance", Double.class),
                            rs.getInt("stake"),
                            rs.getTimestamp("betting_deadline").toInstant(),
                            rs.getTimestamp("resolution_date").toInstant(),
                            BetVisibility.valueOf(rs.getString("visibility")),
                            rs.getBoolean("bet_modifiable"),
                            PorraStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at").toInstant(),
                            bets,
                            currentVersion == null ? 0 : currentVersion);
                },
                porraId.value());
        return porras.stream().findFirst();
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize event", exception);
        }
    }
}
