package com.accierta.groups.infrastructure.persistence;

import com.accierta.groups.application.query.InvitationInfoView;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.out.InvitationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcInvitationRepository implements InvitationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcInvitationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isMember(GroupId groupId, MemberId memberId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from group_members_projection where group_id = ? and user_id = ?",
                Integer.class,
                groupId.value(),
                memberId.value().value());
        return count != null && count > 0;
    }

    @Override
    public boolean existsInvitationForMember(GroupId groupId, String email) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from group_members_projection gm
                join identity_user_projection iu on iu.user_id = gm.user_id
                where gm.group_id = ? and iu.email = ? and gm.left_at is null
                """,
                Integer.class,
                groupId.value(),
                email);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void createInvitation(
            GroupId groupId,
            String invitedEmail,
            String invitationToken,
            Instant expiresAt,
            MemberId invitedBy,
            boolean emailSent,
            Instant occurredAt) {
        long nextVersion = nextVersion(groupId.value());
        jdbcTemplate.update(
                """
                insert into groups_event_store (aggregate_id, version, event_type, payload, occurred_at)
                values (?, ?, ?, cast(? as jsonb), ?)
                """,
                groupId.value(),
                nextVersion,
                "InvitationCreated",
                json("""
                        {
                          "groupId": {"value":"%s"},
                          "invitedEmail": %s,
                          "invitationToken": "%s",
                          "invitedBy": {"value":{"value":"%s"}},
                          "occurredAt": "%s"
                        }
                        """.formatted(
                        groupId.value(),
                        invitedEmail == null ? "null" : "\"" + invitedEmail + "\"",
                        invitationToken,
                        invitedBy.value().value(),
                        occurredAt)),
                Timestamp.from(occurredAt));

        if (emailSent) {
            jdbcTemplate.update(
                    """
                    insert into groups_event_store (aggregate_id, version, event_type, payload, occurred_at)
                    values (?, ?, ?, cast(? as jsonb), ?)
                    """,
                    groupId.value(),
                    nextVersion + 1,
                    "InvitationSent",
                    json("""
                            {
                              "groupId": {"value":"%s"},
                              "invitedEmail": "%s",
                              "invitationToken": "%s",
                              "occurredAt": "%s"
                            }
                            """.formatted(groupId.value(), invitedEmail, invitationToken, occurredAt)),
                    Timestamp.from(occurredAt));
        }

        jdbcTemplate.update(
                """
                insert into group_invitations_projection (
                    invitation_token,
                    group_id,
                    invited_email,
                    invited_by,
                    expires_at,
                    used,
                    used_by,
                    used_at,
                    created_at
                ) values (?, ?, ?, ?, ?, false, null, null, ?)
                """,
                invitationToken,
                groupId.value(),
                invitedEmail,
                invitedBy.value().value(),
                Timestamp.from(expiresAt),
                Timestamp.from(occurredAt));
    }

    @Override
    public Optional<StoredInvitation> findByToken(String invitationToken) {
        List<StoredInvitation> invitations = jdbcTemplate.query(
                """
                select group_id, invited_email, invitation_token, expires_at, invited_by, used
                from group_invitations_projection
                where invitation_token = ?
                """,
                (rs, rowNum) -> new StoredInvitation(
                        rs.getObject("group_id", UUID.class),
                        rs.getString("invited_email"),
                        rs.getString("invitation_token"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getObject("invited_by", UUID.class),
                        rs.getBoolean("used")),
                invitationToken);
        return invitations.stream().findFirst();
    }

    @Override
    @Transactional
    public void markUsed(String invitationToken, MemberId userId, Instant usedAt) {
        UUID groupId = jdbcTemplate.queryForObject(
                "select group_id from group_invitations_projection where invitation_token = ?",
                UUID.class,
                invitationToken);
        long nextVersion = nextVersion(groupId);
        jdbcTemplate.update(
                """
                insert into groups_event_store (aggregate_id, version, event_type, payload, occurred_at)
                values (?, ?, ?, cast(? as jsonb), ?)
                """,
                groupId,
                nextVersion,
                "MemberJoined",
                json("""
                        {
                          "groupId": {"value":"%s"},
                          "memberId": {"value":{"value":"%s"}},
                          "occurredAt": "%s"
                        }
                        """.formatted(groupId, userId.value().value(), usedAt)),
                Timestamp.from(usedAt));

        jdbcTemplate.update(
                """
                update group_invitations_projection
                set used = true, used_by = ?, used_at = ?
                where invitation_token = ?
                """,
                userId.value().value(),
                Timestamp.from(usedAt),
                invitationToken);

        jdbcTemplate.update(
                """
                insert into group_members_projection (group_id, user_id, joined_at)
                values (?, ?, ?)
                on conflict (group_id, user_id) do update set
                    joined_at = excluded.joined_at,
                    left_at = null
                """,
                groupId,
                userId.value().value(),
                Timestamp.from(usedAt));
    }

    @Override
    public Optional<InvitationInfoView> getInfo(String invitationToken, Instant now) {
        List<InvitationInfoView> info = jdbcTemplate.query(
                """
                select g.name as group_name, iu.email as invited_by_email, gi.expires_at, gi.used
                from group_invitations_projection gi
                join groups_projection g on g.group_id = gi.group_id
                join identity_user_projection iu on iu.user_id = gi.invited_by
                where gi.invitation_token = ?
                """,
                (rs, rowNum) -> new InvitationInfoView(
                        rs.getString("group_name"),
                        rs.getString("invited_by_email"),
                        rs.getTimestamp("expires_at").toInstant().isBefore(now),
                        rs.getBoolean("used")),
                invitationToken);
        return info.stream().findFirst();
    }

    @Override
    public boolean groupExists(UUID groupId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from groups_projection where group_id = ?",
                Integer.class,
                groupId);
        return count != null && count > 0;
    }

    private long nextVersion(UUID groupId) {
        Long currentVersion = jdbcTemplate.queryForObject(
                "select coalesce(max(version), 0) from groups_event_store where aggregate_id = ?",
                Long.class,
                groupId);
        return (currentVersion == null ? 0L : currentVersion) + 1;
    }

    private String json(String rawJson) {
        try {
            return objectMapper.readTree(rawJson).toString();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to build invitation event payload", exception);
        }
    }
}
