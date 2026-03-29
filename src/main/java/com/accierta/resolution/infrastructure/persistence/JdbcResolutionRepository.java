package com.accierta.resolution.infrastructure.persistence;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.resolution.domain.event.CancellationProposed;
import com.accierta.resolution.domain.event.CancellationRejected;
import com.accierta.resolution.domain.event.PorraCancelled;
import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.event.ResolutionAutoAccepted;
import com.accierta.resolution.domain.event.ResolutionAccepted;
import com.accierta.resolution.domain.event.ResolutionDisputed;
import com.accierta.resolution.domain.event.ResolutionProposed;
import com.accierta.resolution.domain.event.VoteCast;
import com.accierta.resolution.domain.model.ResolutionMethod;
import com.accierta.resolution.domain.model.Resolution;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.model.ResolutionProposalType;
import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.model.Vote;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import com.accierta.shared.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcResolutionRepository implements ResolutionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcResolutionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(Resolution resolution) {
        List<DomainEvent> newEvents = resolution.pullUncommittedEvents();
        long initialVersion = resolution.version() - newEvents.size();
        for (int index = 0; index < newEvents.size(); index++) {
            DomainEvent event = newEvents.get(index);
            jdbcTemplate.update(
                    """
                    insert into resolution_event_store (aggregate_id, version, event_type, payload, occurred_at)
                    values (?, ?, ?, cast(? as jsonb), ?)
                    """,
                    resolution.id().value(),
                    initialVersion + index + 1,
                    event.getClass().getSimpleName(),
                    serialize(event),
                    Timestamp.from(event.occurredAt()));

            if (event instanceof ResolutionProposed proposed) {
                jdbcTemplate.update(
                        """
                        insert into resolution_projection (
                            resolution_id, porra_id, group_id, proposer_id, proposed_result,
                            waiting_ends_at, status, created_at, accepted_at, disputer_id,
                            dispute_reason, disputed_at, voting_ends_at, accepted_result, accepted_method,
                            proposal_type, proposal_reason
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        proposed.resolutionId().value(),
                        proposed.porraId().value(),
                        proposed.groupId().value(),
                        proposed.proposerId().value(),
                        proposed.proposedResult(),
                        Timestamp.from(proposed.waitingEndsAt()),
                        ResolutionStatus.WAITING.name(),
                        Timestamp.from(proposed.occurredAt()),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        ResolutionProposalType.RESOLUTION.name(),
                        null);
            } else if (event instanceof CancellationProposed proposed) {
                jdbcTemplate.update(
                        """
                        insert into resolution_projection (
                            resolution_id, porra_id, group_id, proposer_id, proposed_result,
                            waiting_ends_at, status, created_at, accepted_at, disputer_id,
                            dispute_reason, disputed_at, voting_ends_at, accepted_result, accepted_method,
                            proposal_type, proposal_reason
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        proposed.resolutionId().value(),
                        proposed.porraId().value(),
                        proposed.groupId().value(),
                        proposed.proposerId().value(),
                        "CANCELLED",
                        Timestamp.from(proposed.waitingEndsAt()),
                        ResolutionStatus.WAITING.name(),
                        Timestamp.from(proposed.occurredAt()),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        ResolutionProposalType.CANCELLATION.name(),
                        proposed.reason());
            } else if (event instanceof ResolutionAutoAccepted accepted) {
                jdbcTemplate.update(
                        """
                        update resolution_projection
                        set status = ?, accepted_at = ?, accepted_result = ?, accepted_method = ?
                        where resolution_id = ?
                        """,
                        ResolutionStatus.ACCEPTED.name(),
                        Timestamp.from(accepted.occurredAt()),
                        accepted.acceptedResult(),
                        ResolutionMethod.CONSENSUS.name(),
                        accepted.resolutionId().value());
            } else if (event instanceof PorraCancelled cancelled) {
                jdbcTemplate.update(
                        """
                        update resolution_projection
                        set status = ?, accepted_at = ?, accepted_result = ?, accepted_method = ?
                        where resolution_id = ?
                        """,
                        ResolutionStatus.ACCEPTED.name(),
                        Timestamp.from(cancelled.occurredAt()),
                        "CANCELLED",
                        cancelled.method().name(),
                        cancelled.resolutionId().value());
            } else if (event instanceof ResolutionDisputed disputed) {
                jdbcTemplate.update(
                        """
                        update resolution_projection
                        set status = ?, disputer_id = ?, dispute_reason = ?, disputed_at = ?, voting_ends_at = ?
                        where resolution_id = ?
                        """,
                        ResolutionStatus.VOTING.name(),
                        disputed.disputerId().value(),
                        disputed.reason(),
                        Timestamp.from(disputed.occurredAt()),
                        Timestamp.from(disputed.votingEndsAt()),
                        disputed.resolutionId().value());
            } else if (event instanceof VoteCast voteCast) {
                jdbcTemplate.update(
                        """
                        insert into resolution_vote_projection (resolution_id, voter_id, voted_result, voted_at)
                        values (?, ?, ?, ?)
                        """,
                        voteCast.resolutionId().value(),
                        voteCast.voterId().value(),
                        voteCast.votedResult(),
                        Timestamp.from(voteCast.occurredAt()));
            } else if (event instanceof ResolutionAccepted accepted) {
                jdbcTemplate.update(
                        """
                        update resolution_projection
                        set status = ?, accepted_at = ?, accepted_result = ?, accepted_method = ?
                        where resolution_id = ?
                        """,
                        ResolutionStatus.ACCEPTED.name(),
                        Timestamp.from(accepted.occurredAt()),
                        accepted.acceptedResult(),
                        accepted.method().name(),
                        accepted.resolutionId().value());
            } else if (event instanceof CancellationRejected rejected) {
                jdbcTemplate.update(
                        """
                        update resolution_projection
                        set status = ?
                        where resolution_id = ?
                        """,
                        ResolutionStatus.REJECTED.name(),
                        rejected.resolutionId().value());
            }
        }
    }

    @Override
    public Optional<Resolution> findByPorraId(PorraId porraId) {
        List<Resolution> resolutions = jdbcTemplate.query(
                "select * from resolution_projection where porra_id = ? order by created_at desc limit 1",
                (rs, rowNum) -> reconstitute(
                        rs.getObject("resolution_id", UUID.class),
                        rs.getObject("porra_id", UUID.class),
                        rs.getObject("group_id", UUID.class),
                        rs.getObject("proposer_id", UUID.class),
                        rs.getString("proposed_result"),
                        rs.getTimestamp("waiting_ends_at").toInstant(),
                        ResolutionStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("accepted_at") == null ? null : rs.getTimestamp("accepted_at").toInstant(),
                        rs.getObject("disputer_id", UUID.class),
                        rs.getString("dispute_reason"),
                        rs.getTimestamp("disputed_at") == null ? null : rs.getTimestamp("disputed_at").toInstant(),
                        rs.getTimestamp("voting_ends_at") == null ? null : rs.getTimestamp("voting_ends_at").toInstant(),
                        rs.getString("accepted_result"),
                        rs.getString("accepted_method"),
                        rs.getString("proposal_type"),
                        rs.getString("proposal_reason")),
                porraId.value());
        return resolutions.stream().findFirst();
    }

    @Override
    public Optional<Resolution> findById(ResolutionId resolutionId) {
        List<Resolution> resolutions = jdbcTemplate.query(
                "select * from resolution_projection where resolution_id = ?",
                (rs, rowNum) -> reconstitute(
                        rs.getObject("resolution_id", UUID.class),
                        rs.getObject("porra_id", UUID.class),
                        rs.getObject("group_id", UUID.class),
                        rs.getObject("proposer_id", UUID.class),
                        rs.getString("proposed_result"),
                        rs.getTimestamp("waiting_ends_at").toInstant(),
                        ResolutionStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("accepted_at") == null ? null : rs.getTimestamp("accepted_at").toInstant(),
                        rs.getObject("disputer_id", UUID.class),
                        rs.getString("dispute_reason"),
                        rs.getTimestamp("disputed_at") == null ? null : rs.getTimestamp("disputed_at").toInstant(),
                        rs.getTimestamp("voting_ends_at") == null ? null : rs.getTimestamp("voting_ends_at").toInstant(),
                        rs.getString("accepted_result"),
                        rs.getString("accepted_method"),
                        rs.getString("proposal_type"),
                        rs.getString("proposal_reason")),
                resolutionId.value());
        return resolutions.stream().findFirst();
    }

    private Resolution reconstitute(
            UUID resolutionId,
            UUID porraId,
            UUID groupId,
            UUID proposerId,
            String proposedResult,
            java.time.Instant waitingEndsAt,
            ResolutionStatus status,
            java.time.Instant createdAt,
            java.time.Instant acceptedAt,
            UUID disputerId,
            String disputeReason,
            java.time.Instant disputedAt,
            java.time.Instant votingEndsAt,
            String acceptedResult,
            String acceptedMethod,
            String proposalType,
            String proposalReason) {
        Long version = jdbcTemplate.queryForObject(
                "select coalesce(max(version), 0) from resolution_event_store where aggregate_id = ?",
                Long.class,
                resolutionId);
        List<Vote> votes = jdbcTemplate.query(
                """
                select voter_id, voted_result, voted_at
                from resolution_vote_projection
                where resolution_id = ?
                order by voted_at asc
                """,
                (rs, rowNum) -> new Vote(
                        new UserId(rs.getObject("voter_id", UUID.class)),
                        rs.getString("voted_result"),
                        rs.getTimestamp("voted_at").toInstant()),
                resolutionId);
        return Resolution.reconstitute(
                new ResolutionId(resolutionId),
                new PorraId(porraId),
                new GroupId(groupId),
                new UserId(proposerId),
                proposedResult,
                waitingEndsAt,
                status,
                createdAt,
                acceptedAt,
                disputerId == null ? null : new UserId(disputerId),
                disputeReason,
                disputedAt,
                votingEndsAt,
                acceptedResult,
                acceptedMethod == null ? null : ResolutionMethod.valueOf(acceptedMethod),
                proposalType == null ? ResolutionProposalType.RESOLUTION : ResolutionProposalType.valueOf(proposalType),
                proposalReason,
                votes,
                version == null ? 0 : version);
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize event", exception);
        }
    }
}
