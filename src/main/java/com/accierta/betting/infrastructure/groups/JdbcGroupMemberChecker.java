package com.accierta.betting.infrastructure.groups;

import com.accierta.betting.domain.port.out.GroupMemberChecker;
import com.accierta.identity.domain.model.UserId;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcGroupMemberChecker implements GroupMemberChecker {

    private final JdbcTemplate jdbcTemplate;

    public JdbcGroupMemberChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isActiveMember(UUID groupId, UserId userId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from group_members_projection
                where group_id = ? and user_id = ? and left_at is null
                """,
                Integer.class,
                groupId,
                userId.value());
        return count != null && count > 0;
    }
}
