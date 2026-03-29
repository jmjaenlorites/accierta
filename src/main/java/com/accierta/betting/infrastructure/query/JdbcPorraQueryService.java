package com.accierta.betting.infrastructure.query;

import com.accierta.betting.application.query.PorraDetailView;
import com.accierta.betting.application.query.PorraQueryService;
import com.accierta.betting.application.query.PorraSummaryView;
import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.identity.domain.model.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcPorraQueryService implements PorraQueryService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPorraQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PorraSummaryView> listGroupPorras(UUID groupId, PorraStatus status, UserId requesterId) {
        if (!isActiveMember(groupId, requesterId)) {
            return List.of();
        }
        String sql = """
                select porra_id, title, type, status, stake, betting_deadline
                from porra_projection
                where group_id = ?
                """;
        Object[] params;
        if (status != null) {
            sql += " and status = ?";
            params = new Object[] {groupId, status.name()};
        } else {
            params = new Object[] {groupId};
        }
        sql += " order by created_at desc";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PorraSummaryView(
                        rs.getObject("porra_id", UUID.class),
                        rs.getString("title"),
                        PorraType.valueOf(rs.getString("type")),
                        PorraStatus.valueOf(rs.getString("status")),
                        rs.getInt("stake"),
                        rs.getTimestamp("betting_deadline").toInstant(),
                        countBets(rs.getObject("porra_id", UUID.class))),
                params);
    }

    @Override
    public Optional<PorraDetailView> findPorra(UUID porraId, UserId requesterId) {
        List<PorraDetailView> views = jdbcTemplate.query(
                "select * from porra_projection where porra_id = ?",
                (rs, rowNum) -> {
                    UUID groupId = rs.getObject("group_id", UUID.class);
                    if (!isActiveMember(groupId, requesterId)) {
                        return null;
                    }
                    List<String> options = jdbcTemplate.query(
                            "select option_value from porra_option_projection where porra_id = ? order by option_value",
                            (ors, idx) -> ors.getString("option_value"),
                            porraId);
                    boolean hiddenAndOpen =
                            BetVisibility.valueOf(rs.getString("visibility")) == BetVisibility.HIDDEN
                                    && PorraStatus.valueOf(rs.getString("status")) == PorraStatus.OPEN;
                    List<PorraDetailView.BetView> bets = hiddenAndOpen
                            ? List.of()
                            : jdbcTemplate.query(
                                    "select member_id, prediction, placed_at from porra_bet_projection where porra_id = ? order by placed_at asc",
                                    (brs, idx) -> new PorraDetailView.BetView(
                                            brs.getObject("member_id", UUID.class),
                                            brs.getString("prediction"),
                                            brs.getTimestamp("placed_at").toInstant()),
                                    porraId);
                    return new PorraDetailView(
                            rs.getObject("porra_id", UUID.class),
                            groupId,
                            rs.getObject("creator_id", UUID.class),
                            rs.getString("title"),
                            rs.getString("description"),
                            PorraType.valueOf(rs.getString("type")),
                            options,
                            rs.getObject("tolerance", Double.class),
                            rs.getInt("stake"),
                            rs.getTimestamp("betting_deadline").toInstant(),
                            rs.getTimestamp("resolution_date").toInstant(),
                            BetVisibility.valueOf(rs.getString("visibility")),
                            rs.getBoolean("bet_modifiable"),
                            PorraStatus.valueOf(rs.getString("status")),
                            bets,
                            rs.getTimestamp("created_at").toInstant());
                },
                porraId).stream().filter(java.util.Objects::nonNull).toList();
        return views.stream().findFirst();
    }

    private int countBets(UUID porraId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from porra_bet_projection where porra_id = ?",
                Integer.class,
                porraId);
        return count == null ? 0 : count;
    }

    private boolean isActiveMember(UUID groupId, UserId requesterId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from group_members_projection
                where group_id = ? and user_id = ? and left_at is null
                """,
                Integer.class,
                groupId,
                requesterId.value());
        return count != null && count > 0;
    }
}
