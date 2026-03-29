package com.accierta.identity.infrastructure.persistence;

import com.accierta.identity.domain.model.RefreshToken;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRefreshTokenRepository implements RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRefreshTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(RefreshToken refreshToken) {
        jdbcTemplate.update(
                """
                insert into identity_refresh_tokens (
                    token,
                    user_id,
                    expires_at,
                    invalidated,
                    invalidated_at
                ) values (?, ?, ?, ?, ?)
                on conflict (token) do update set
                    invalidated = excluded.invalidated,
                    invalidated_at = excluded.invalidated_at,
                    expires_at = excluded.expires_at
                """,
                refreshToken.value(),
                refreshToken.userId().value(),
                Timestamp.from(refreshToken.expiresAt()),
                refreshToken.invalidated(),
                null);
    }

    @Override
    public Optional<RefreshToken> findByValue(String value) {
        List<RefreshToken> tokens = jdbcTemplate.query(
                "select token, user_id, expires_at, invalidated from identity_refresh_tokens where token = ?",
                this::mapRefreshToken,
                value);
        return tokens.stream().findFirst();
    }

    @Override
    public void invalidate(String value, Instant invalidatedAt) {
        jdbcTemplate.update(
                """
                update identity_refresh_tokens
                set invalidated = true, invalidated_at = ?
                where token = ?
                """,
                Timestamp.from(invalidatedAt),
                value);
    }

    @Override
    public void invalidateAllForUser(UserId userId, Instant invalidatedAt) {
        jdbcTemplate.update(
                """
                update identity_refresh_tokens
                set invalidated = true, invalidated_at = ?
                where user_id = ?
                """,
                Timestamp.from(invalidatedAt),
                userId.value());
    }

    private RefreshToken mapRefreshToken(ResultSet rs, int rowNum) throws SQLException {
        return new RefreshToken(
                rs.getString("token"),
                new UserId(rs.getObject("user_id", UUID.class)),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getBoolean("invalidated"));
    }
}
