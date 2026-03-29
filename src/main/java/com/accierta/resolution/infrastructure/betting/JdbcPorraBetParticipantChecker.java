package com.accierta.resolution.infrastructure.betting;

import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcPorraBetParticipantChecker implements PorraBetParticipantChecker {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPorraBetParticipantChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean hasBet(UUID porraId, UserId userId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from porra_bet_projection
                where porra_id = ? and member_id = ?
                """,
                Integer.class,
                porraId,
                userId.value());
        return count != null && count > 0;
    }
}
