package com.accierta.identity.infrastructure.persistence;

import com.accierta.identity.domain.event.LoginFailed;
import com.accierta.identity.domain.port.out.AuthenticationFailureLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcAuthenticationFailureLogger implements AuthenticationFailureLogger {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcAuthenticationFailureLogger(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void log(LoginFailed event) {
        jdbcTemplate.update(
                """
                insert into identity_login_failed_events (email, reason, payload, occurred_at)
                values (?, ?, cast(? as jsonb), ?)
                """,
                event.email().value(),
                event.reason(),
                serialize(event),
                Timestamp.from(event.occurredAt()));
    }

    private String serialize(LoginFailed event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize login failed event", exception);
        }
    }
}
