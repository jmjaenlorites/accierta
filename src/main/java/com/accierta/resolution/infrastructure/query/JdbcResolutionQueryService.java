package com.accierta.resolution.infrastructure.query;

import com.accierta.resolution.application.query.ResolutionQueryService;
import com.accierta.resolution.application.query.ResolutionView;
import com.accierta.resolution.application.query.ResolutionVotesView;
import com.accierta.resolution.domain.model.ResolutionStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcResolutionQueryService implements ResolutionQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public JdbcResolutionQueryService(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public Optional<ResolutionView> findByPorraId(UUID porraId) {
        List<ResolutionView> views = jdbcTemplate.query(
                """
                select r.resolution_id, r.proposed_result, r.waiting_ends_at, r.voting_ends_at, r.status,
                       u.user_id as proposed_by_id, u.email as proposed_by_email
                from resolution_projection r
                join identity_user_projection u on u.user_id = r.proposer_id
                where r.porra_id = ?
                order by r.created_at desc
                limit 1
                """,
                (rs, rowNum) -> {
                    ResolutionStatus status = ResolutionStatus.valueOf(rs.getString("status"));
                    Instant deadline = status == ResolutionStatus.VOTING && rs.getTimestamp("voting_ends_at") != null
                            ? rs.getTimestamp("voting_ends_at").toInstant()
                            : rs.getTimestamp("waiting_ends_at").toInstant();
                    Duration timeRemaining = Duration.between(Instant.now(clock), deadline);
                    if (timeRemaining.isNegative()) {
                        timeRemaining = Duration.ZERO;
                    }
                    return new ResolutionView(
                            rs.getObject("resolution_id", UUID.class),
                            rs.getString("proposed_result"),
                            new ResolutionView.ProposedByView(
                                    rs.getObject("proposed_by_id", UUID.class),
                                    rs.getString("proposed_by_email")),
                            deadline,
                            status,
                            timeRemaining);
                },
                porraId);
        return views.stream().findFirst();
    }

    @Override
    public Optional<ResolutionVotesView> findVotesByPorraId(UUID porraId) {
        List<ResolutionVotesView> views = jdbcTemplate.query(
                """
                select resolution_id, proposer_id, disputer_id, voting_ends_at, status
                from resolution_projection
                where porra_id = ?
                order by created_at desc
                limit 1
                """,
                (rs, rowNum) -> {
                    UUID resolutionId = rs.getObject("resolution_id", UUID.class);
                    UUID proposerId = rs.getObject("proposer_id", UUID.class);
                    UUID disputerId = rs.getObject("disputer_id", UUID.class);
                    Instant votingEndsAt = rs.getTimestamp("voting_ends_at") == null
                            ? null
                            : rs.getTimestamp("voting_ends_at").toInstant();
                    ResolutionStatus status = ResolutionStatus.valueOf(rs.getString("status"));
                    List<ResolutionVotesView.VoteView> votes = jdbcTemplate.query(
                            """
                            select voter_id, voted_result
                            from resolution_vote_projection
                            where resolution_id = ?
                            order by voted_at asc
                            """,
                            (vrs, idx) -> new ResolutionVotesView.VoteView(
                                    vrs.getObject("voter_id", UUID.class),
                                    vrs.getString("voted_result")),
                            resolutionId);
                    Integer bettorCount = jdbcTemplate.queryForObject(
                            "select count(*) from porra_bet_projection where porra_id = ?",
                            Integer.class,
                            porraId);
                    int eligibleVoters = bettorCount == null ? 0 : bettorCount;
                    if (proposerId != null) {
                        eligibleVoters--;
                    }
                    if (disputerId != null) {
                        eligibleVoters--;
                    }
                    if (eligibleVoters < 0) {
                        eligibleVoters = 0;
                    }
                    Duration timeRemaining = votingEndsAt == null
                            ? Duration.ZERO
                            : Duration.between(Instant.now(clock), votingEndsAt);
                    if (timeRemaining.isNegative()) {
                        timeRemaining = Duration.ZERO;
                    }
                    return new ResolutionVotesView(
                            status,
                            votingEndsAt,
                            eligibleVoters,
                            votes.size(),
                            timeRemaining,
                            votes);
                },
                porraId);
        return views.stream().findFirst();
    }
}
