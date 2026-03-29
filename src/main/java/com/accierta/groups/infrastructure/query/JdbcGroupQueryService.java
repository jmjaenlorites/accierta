package com.accierta.groups.infrastructure.query;

import com.accierta.groups.application.query.GroupDetailView;
import com.accierta.groups.application.query.GroupMemberView;
import com.accierta.groups.application.query.GroupQueryService;
import com.accierta.groups.application.query.GroupSummaryView;
import com.accierta.identity.domain.model.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcGroupQueryService implements GroupQueryService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcGroupQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<GroupSummaryView> listUserGroups(UserId userId) {
        return jdbcTemplate.query(
                """
                select g.group_id, g.name, g.currency, g.created_at, count(m.user_id) as member_count
                from groups_projection g
                join group_members_projection mine on mine.group_id = g.group_id and mine.user_id = ? and mine.left_at is null
                join group_members_projection m on m.group_id = g.group_id and m.left_at is null
                group by g.group_id, g.name, g.currency, g.created_at
                order by g.created_at desc
                """,
                (rs, rowNum) -> new GroupSummaryView(
                        rs.getObject("group_id", UUID.class),
                        rs.getString("name"),
                        rs.getString("currency"),
                        rs.getInt("member_count"),
                        rs.getTimestamp("created_at").toInstant()),
                userId.value());
    }

    @Override
    public Optional<GroupDetailView> findGroup(UUID groupId, UserId userId) {
        Integer membershipCount = jdbcTemplate.queryForObject(
                "select count(*) from group_members_projection where group_id = ? and user_id = ? and left_at is null",
                Integer.class,
                groupId,
                userId.value());
        boolean isMember = membershipCount != null && membershipCount > 0;
        if (!isMember) {
            return Optional.empty();
        }

        List<GroupDetailView> details = jdbcTemplate.query(
                "select group_id, name, currency, created_at from groups_projection where group_id = ?",
                (rs, rowNum) -> new GroupDetailView(
                        rs.getObject("group_id", UUID.class),
                        rs.getString("name"),
                        rs.getString("currency"),
                        loadMembers(groupId),
                        rs.getTimestamp("created_at").toInstant()),
                groupId);
        return details.stream().findFirst();
    }

    @Override
    public boolean existsById(UUID groupId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from groups_projection where group_id = ?",
                Integer.class,
                groupId);
        return count != null && count > 0;
    }

    private List<GroupMemberView> loadMembers(UUID groupId) {
        return jdbcTemplate.query(
                """
                select gm.user_id, gm.joined_at, iu.email
                from group_members_projection gm
                join identity_user_projection iu on iu.user_id = gm.user_id
                where gm.group_id = ? and gm.left_at is null
                order by gm.joined_at asc
                """,
                this::mapGroupMember,
                groupId);
    }

    private GroupMemberView mapGroupMember(ResultSet rs, int rowNum) throws SQLException {
        return new GroupMemberView(
                rs.getObject("user_id", UUID.class),
                rs.getString("email"),
                rs.getTimestamp("joined_at").toInstant());
    }
}
