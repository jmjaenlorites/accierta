package com.accierta.resolution.infrastructure.betting;

import com.accierta.betting.domain.event.PorraResolved;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.resolution.domain.port.out.ResolvedPorraMarker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JdbcResolvedPorraMarker implements ResolvedPorraMarker {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcResolvedPorraMarker(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void markResolved(UUID porraId, Instant occurredAt) {
        String currentStatus = jdbcTemplate.queryForObject(
                "select status from porra_projection where porra_id = ?",
                String.class,
                porraId);
        if (PorraStatus.RESOLVED.name().equals(currentStatus)) {
            return;
        }
        Long version = jdbcTemplate.queryForObject(
                "select coalesce(max(version), 0) from porra_event_store where aggregate_id = ?",
                Long.class,
                porraId);
        PorraResolved event = new PorraResolved(new com.accierta.betting.domain.model.PorraId(porraId), occurredAt);
        jdbcTemplate.update(
                """
                insert into porra_event_store (aggregate_id, version, event_type, payload, occurred_at)
                values (?, ?, ?, cast(? as jsonb), ?)
                """,
                porraId,
                (version == null ? 0 : version) + 1,
                event.getClass().getSimpleName(),
                serialize(event),
                Timestamp.from(occurredAt));
        jdbcTemplate.update(
                "update porra_projection set status = ? where porra_id = ?",
                PorraStatus.RESOLVED.name(),
                porraId);
    }

    private String serialize(PorraResolved event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize event", exception);
        }
    }
}
