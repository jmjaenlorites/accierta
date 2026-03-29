package com.accierta.groups.infrastructure.persistence;

import com.accierta.groups.domain.port.out.VerifiedUserChecker;
import com.accierta.identity.domain.model.UserId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcVerifiedUserChecker implements VerifiedUserChecker {

    private final JdbcTemplate jdbcTemplate;

    public JdbcVerifiedUserChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isVerified(UserId userId) {
        Boolean verified = jdbcTemplate.query(
                        "select verified from identity_user_projection where user_id = ?",
                        rs -> rs.next() ? rs.getBoolean("verified") : null,
                        userId.value());
        return Boolean.TRUE.equals(verified);
    }
}
