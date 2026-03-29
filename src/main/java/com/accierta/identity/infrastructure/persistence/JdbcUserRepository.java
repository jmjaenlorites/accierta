package com.accierta.identity.infrastructure.persistence;

import com.accierta.identity.domain.event.EmailVerified;
import com.accierta.identity.domain.event.PasswordReset;
import com.accierta.identity.domain.event.PasswordResetRequested;
import com.accierta.identity.domain.event.UserRegistered;
import com.accierta.identity.domain.event.VerificationEmailResent;
import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.ResetToken;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.model.VerificationToken;
import com.accierta.identity.domain.port.out.UserRepository;
import com.accierta.shared.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean existsByEmail(Email email) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from identity_user_projection where email = ?",
                Integer.class,
                email.value());
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void save(User user) {
        List<DomainEvent> newEvents = user.pullUncommittedEvents();
        long initialVersion = user.version() - newEvents.size();

        for (int index = 0; index < newEvents.size(); index++) {
            DomainEvent event = newEvents.get(index);
            jdbcTemplate.update(
                    """
                    insert into identity_event_store (
                        aggregate_id,
                        version,
                        event_type,
                        payload,
                        occurred_at
                    ) values (?, ?, ?, cast(? as jsonb), ?)
                    """,
                    user.id().value(),
                    initialVersion + index + 1,
                    event.getClass().getSimpleName(),
                    serialize(event),
                    Timestamp.from(event.occurredAt()));
        }

        jdbcTemplate.update(
                """
                insert into identity_user_projection (
                    user_id,
                    email,
                    hashed_password,
                    verified,
                    invitation_token,
                    verification_token,
                    verification_token_expires_at,
                    reset_token,
                    reset_token_expires_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (user_id) do update set
                    email = excluded.email,
                    hashed_password = excluded.hashed_password,
                    verified = excluded.verified,
                    invitation_token = excluded.invitation_token,
                    verification_token = excluded.verification_token,
                    verification_token_expires_at = excluded.verification_token_expires_at,
                    reset_token = excluded.reset_token,
                    reset_token_expires_at = excluded.reset_token_expires_at
                """,
                user.id().value(),
                user.email().value(),
                user.hashedPassword().value(),
                user.isVerified(),
                user.invitationToken(),
                user.verificationToken() == null ? null : user.verificationToken().value(),
                user.verificationToken() == null ? null : Timestamp.from(user.verificationToken().expiresAt()),
                user.resetToken() == null ? null : user.resetToken().value(),
                user.resetToken() == null ? null : Timestamp.from(user.resetToken().expiresAt()));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return findUserState(
                "select * from identity_user_projection where email = ?",
                email.value());
    }

    @Override
    public Optional<User> findByVerificationToken(String verificationToken) {
        return findUserState(
                "select * from identity_user_projection where verification_token = ?",
                verificationToken);
    }

    @Override
    public Optional<User> findByResetToken(String resetToken) {
        return findUserState(
                "select * from identity_user_projection where reset_token = ?",
                resetToken);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return findUserState(
                "select * from identity_user_projection where user_id = ?",
                userId.value());
    }

    private Optional<User> findUserState(String sql, Object parameter) {
        List<User> users = jdbcTemplate.query(sql, this::mapUser, parameter);
        return users.stream().findFirst();
    }

    private User mapUser(ResultSet rs, int rowNum) throws SQLException {
        UUID userId = rs.getObject("user_id", UUID.class);
        List<DomainEvent> history = loadHistory(userId);
        String verificationTokenValue = rs.getString("verification_token");
        Timestamp verificationTokenExpiresAt = rs.getTimestamp("verification_token_expires_at");
        VerificationToken verificationToken = verificationTokenValue == null
                ? null
                : new VerificationToken(verificationTokenValue, verificationTokenExpiresAt.toInstant());
        String resetTokenValue = rs.getString("reset_token");
        Timestamp resetTokenExpiresAt = rs.getTimestamp("reset_token_expires_at");
        ResetToken resetToken = resetTokenValue == null ? null : new ResetToken(resetTokenValue, resetTokenExpiresAt.toInstant());

        return User.reconstitute(
                history,
                new HashedPassword(rs.getString("hashed_password")),
                verificationToken,
                resetToken);
    }

    private List<DomainEvent> loadHistory(UUID aggregateId) {
        return jdbcTemplate.query(
                """
                select aggregate_id, version, event_type, payload, occurred_at
                from identity_event_store
                where aggregate_id = ?
                order by version asc
                """,
                userEventRowMapper(),
                aggregateId).stream()
                .map(this::deserialize)
                .toList();
    }

    private RowMapper<UserEventRecord> userEventRowMapper() {
        return (rs, rowNum) -> new UserEventRecord(
                rs.getObject("aggregate_id", UUID.class),
                rs.getLong("version"),
                rs.getString("event_type"),
                rs.getString("payload"),
                rs.getTimestamp("occurred_at").toInstant());
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize event", exception);
        }
    }

    private DomainEvent deserialize(UserEventRecord eventRecord) {
        try {
            return switch (eventRecord.eventType()) {
                case "UserRegistered" -> objectMapper.readValue(eventRecord.payload(), UserRegistered.class);
                case "EmailVerified" -> objectMapper.readValue(eventRecord.payload(), EmailVerified.class);
                case "VerificationEmailResent" ->
                        objectMapper.readValue(eventRecord.payload(), VerificationEmailResent.class);
                case "PasswordResetRequested" ->
                        objectMapper.readValue(eventRecord.payload(), PasswordResetRequested.class);
                case "PasswordReset" -> objectMapper.readValue(eventRecord.payload(), PasswordReset.class);
                default -> throw new IllegalStateException("unsupported event type: " + eventRecord.eventType());
            };
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize event", exception);
        }
    }
}
