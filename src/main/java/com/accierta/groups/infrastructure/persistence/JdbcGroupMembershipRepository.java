package com.accierta.groups.infrastructure.persistence;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.out.GroupMembershipRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcGroupMembershipRepository implements GroupMembershipRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcGroupMembershipRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isActiveMember(GroupId groupId, MemberId memberId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from group_members_projection
                where group_id = ? and user_id = ? and left_at is null
                """,
                Integer.class,
                groupId.value(),
                memberId.value().value());
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void markLeft(GroupId groupId, MemberId memberId, Instant leftAt) {
        jdbcTemplate.update(
                """
                update group_members_projection
                set left_at = ?
                where group_id = ? and user_id = ?
                """,
                Timestamp.from(leftAt),
                groupId.value(),
                memberId.value().value());
    }

    @Override
    @Transactional
    public void appendMemberLeftEvent(GroupId groupId, MemberId memberId, int pendingDebtsOwed, int pendingDebtsOwing, Instant occurredAt) {
        Long currentVersion = jdbcTemplate.queryForObject(
                "select coalesce(max(version), 0) from groups_event_store where aggregate_id = ?",
                Long.class,
                groupId.value());
        long nextVersion = (currentVersion == null ? 0L : currentVersion) + 1;
        jdbcTemplate.update(
                """
                insert into groups_event_store (aggregate_id, version, event_type, payload, occurred_at)
                values (?, ?, ?, cast(? as jsonb), ?)
                """,
                groupId.value(),
                nextVersion,
                "MemberLeft",
                json("""
                        {
                          "groupId": {"value":"%s"},
                          "memberId": {"value":{"value":"%s"}},
                          "pendingDebtsOwed": %s,
                          "pendingDebtsOwing": %s,
                          "occurredAt": "%s"
                        }
                        """.formatted(
                        groupId.value(),
                        memberId.value().value(),
                        pendingDebtsOwed,
                        pendingDebtsOwing,
                        occurredAt)),
                Timestamp.from(occurredAt));
    }

    private String json(String rawJson) {
        try {
            return objectMapper.readTree(rawJson).toString();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize member left event", exception);
        }
    }
}
