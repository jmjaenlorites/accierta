package com.accierta.resolution.domain.model;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.event.CancellationProposed;
import com.accierta.resolution.domain.event.CancellationRejected;
import com.accierta.resolution.domain.event.PorraCancelled;
import com.accierta.resolution.domain.event.ResolutionAutoAccepted;
import com.accierta.resolution.domain.event.ResolutionAccepted;
import com.accierta.resolution.domain.event.ResolutionDisputed;
import com.accierta.resolution.domain.event.ResolutionProposed;
import com.accierta.resolution.domain.event.VoteCast;
import com.accierta.shared.domain.AggregateRoot;
import com.accierta.shared.domain.DomainEvent;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Resolution extends AggregateRoot {

    private static final Duration WAITING_PERIOD = Duration.ofHours(48);
    private static final String NONE_OF_THE_ABOVE = "NONE_OF_THE_ABOVE";
    private static final String CANCELLED_RESULT = "CANCELLED";
    private static final String KEEP_RESULT = "KEEP";

    private ResolutionId id;
    private PorraId porraId;
    private GroupId groupId;
    private UserId proposerId;
    private String proposedResult;
    private Instant waitingEndsAt;
    private ResolutionStatus status;
    private Instant createdAt;
    private Instant acceptedAt;
    private UserId disputerId;
    private String disputeReason;
    private Instant disputedAt;
    private Instant votingEndsAt;
    private String acceptedResult;
    private ResolutionMethod acceptedMethod;
    private ResolutionProposalType proposalType;
    private String proposalReason;
    private final Map<UserId, Vote> votes = new HashMap<>();

    private Resolution() {
    }

    public static Resolution propose(
            ResolutionId resolutionId,
            PorraId porraId,
            GroupId groupId,
            UserId proposerId,
            PorraType porraType,
            List<String> options,
            String rawProposedResult,
            Instant proposedAt) {
        Objects.requireNonNull(resolutionId, "resolutionId cannot be null");
        Objects.requireNonNull(porraId, "porraId cannot be null");
        Objects.requireNonNull(groupId, "groupId cannot be null");
        Objects.requireNonNull(proposerId, "proposerId cannot be null");
        Objects.requireNonNull(porraType, "porraType cannot be null");
        Objects.requireNonNull(rawProposedResult, "proposedResult cannot be null");
        Objects.requireNonNull(proposedAt, "proposedAt cannot be null");

        Resolution resolution = new Resolution();
        String normalizedResult = normalizeResult(porraType, options, rawProposedResult);
        resolution.emit(new ResolutionProposed(
                resolutionId,
                porraId,
                groupId,
                proposerId,
                normalizedResult,
                proposedAt.plus(WAITING_PERIOD),
                proposedAt));
        return resolution;
    }

    public static Resolution proposeCancellation(
            ResolutionId resolutionId,
            PorraId porraId,
            GroupId groupId,
            UserId proposerId,
            String reason,
            Instant proposedAt) {
        Objects.requireNonNull(resolutionId, "resolutionId cannot be null");
        Objects.requireNonNull(porraId, "porraId cannot be null");
        Objects.requireNonNull(groupId, "groupId cannot be null");
        Objects.requireNonNull(proposerId, "proposerId cannot be null");
        Objects.requireNonNull(proposedAt, "proposedAt cannot be null");

        Resolution resolution = new Resolution();
        resolution.emit(new CancellationProposed(
                resolutionId,
                porraId,
                groupId,
                proposerId,
                reason == null || reason.isBlank() ? null : reason.trim(),
                proposedAt.plus(WAITING_PERIOD),
                proposedAt));
        return resolution;
    }

    public void autoAccept(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        if (status != ResolutionStatus.WAITING) {
            return;
        }
        if (waitingEndsAt.isAfter(now)) {
            return;
        }
        if (proposalType == ResolutionProposalType.CANCELLATION) {
            emit(new PorraCancelled(id, porraId, ResolutionMethod.CONSENSUS, waitingEndsAt));
            return;
        }
        emit(new ResolutionAutoAccepted(id, porraId, proposedResult, waitingEndsAt));
    }

    public void dispute(UserId requesterId, String reason, Instant now) {
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (status != ResolutionStatus.WAITING) {
            throw new IllegalStateException("no active proposal");
        }
        if (waitingEndsAt.isBefore(now)) {
            throw new IllegalStateException("no active proposal");
        }
        if (requesterId.equals(proposerId)) {
            throw new IllegalArgumentException("proposer cannot dispute");
        }
        emit(new ResolutionDisputed(
                id,
                porraId,
                requesterId,
                reason == null || reason.isBlank() ? null : reason.trim(),
                now.plus(WAITING_PERIOD),
                now));
    }

    public void castVote(
            UserId voterId,
            PorraType porraType,
            List<String> options,
            String rawVotedResult,
            Instant now) {
        Objects.requireNonNull(voterId, "voterId cannot be null");
        Objects.requireNonNull(porraType, "porraType cannot be null");
        Objects.requireNonNull(rawVotedResult, "votedResult cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (status != ResolutionStatus.VOTING || votingEndsAt == null || !votingEndsAt.isAfter(now)) {
            throw new IllegalStateException("voting closed");
        }
        if (voterId.equals(proposerId)) {
            throw new IllegalArgumentException("proposer cannot vote");
        }
        if (disputerId != null && voterId.equals(disputerId)) {
            throw new IllegalArgumentException("disputer cannot vote");
        }
        if (votes.containsKey(voterId)) {
            throw new IllegalArgumentException("already voted");
        }
        String normalizedVote = proposalType == ResolutionProposalType.CANCELLATION
                ? normalizeCancellationVote(rawVotedResult)
                : normalizeResult(porraType, options, rawVotedResult);
        emit(new VoteCast(id, voterId, normalizedVote, now));
    }

    public void tallyVotes(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        if (status != ResolutionStatus.VOTING) {
            return;
        }
        if (votingEndsAt.isAfter(now)) {
            return;
        }
        if (votes.isEmpty()) {
            if (proposalType == ResolutionProposalType.CANCELLATION) {
                emit(new PorraCancelled(id, porraId, ResolutionMethod.CONSENSUS, votingEndsAt));
            } else {
                emit(new ResolutionAccepted(id, porraId, proposedResult, ResolutionMethod.CONSENSUS, votingEndsAt));
            }
            return;
        }
        Map<String, Long> counts = votes.values().stream()
                .collect(Collectors.groupingBy(Vote::votedResult, Collectors.counting()));
        long maxVotes = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<String> winners = counts.entrySet().stream()
                .filter(entry -> entry.getValue() == maxVotes)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
        if (winners.size() == 1) {
            if (proposalType == ResolutionProposalType.CANCELLATION) {
                if (CANCELLED_RESULT.equals(winners.getFirst())) {
                    emit(new PorraCancelled(id, porraId, ResolutionMethod.VOTE, votingEndsAt));
                } else {
                    emit(new CancellationRejected(id, porraId, votingEndsAt));
                }
            } else {
                emit(new ResolutionAccepted(id, porraId, winners.getFirst(), ResolutionMethod.VOTE, votingEndsAt));
            }
            return;
        }
        List<Vote> tiedVotes = votes.values().stream()
                .filter(vote -> winners.contains(vote.votedResult()))
                .toList();
        String selectedResult = tiedVotes.isEmpty()
                ? proposedResult
                : tiedVotes.get(ThreadLocalRandom.current().nextInt(tiedVotes.size())).votedResult();
        if (proposalType == ResolutionProposalType.CANCELLATION) {
            if (CANCELLED_RESULT.equals(selectedResult)) {
                emit(new PorraCancelled(id, porraId, ResolutionMethod.TIEBREAK, votingEndsAt));
            } else {
                emit(new CancellationRejected(id, porraId, votingEndsAt));
            }
        } else {
            emit(new ResolutionAccepted(id, porraId, selectedResult, ResolutionMethod.TIEBREAK, votingEndsAt));
        }
    }

    private static String normalizeCancellationVote(String rawVotedResult) {
        String trimmed = rawVotedResult.trim();
        if (CANCELLED_RESULT.equalsIgnoreCase(trimmed)) {
            return CANCELLED_RESULT;
        }
        if (KEEP_RESULT.equalsIgnoreCase(trimmed)) {
            return KEEP_RESULT;
        }
        throw new IllegalArgumentException("invalid cancellation vote");
    }

    private static String normalizeResult(PorraType porraType, List<String> options, String rawProposedResult) {
        String trimmed = rawProposedResult.trim();
        return switch (porraType) {
            case BINARY -> {
                if (!"true".equalsIgnoreCase(trimmed) && !"false".equalsIgnoreCase(trimmed)) {
                    throw new IllegalArgumentException("invalid binary result");
                }
                yield trimmed.toLowerCase();
            }
            case NUMERIC -> {
                try {
                    new BigDecimal(trimmed);
                    yield trimmed;
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("invalid numeric result");
                }
            }
            case MULTIPLE_CHOICE -> {
                if (NONE_OF_THE_ABOVE.equals(trimmed)) {
                    yield trimmed;
                }
                if (options == null || options.stream().noneMatch(option -> option.equals(trimmed))) {
                    throw new IllegalArgumentException("invalid option");
                }
                yield trimmed;
            }
        };
    }

    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof ResolutionProposed proposed) {
            id = proposed.resolutionId();
            porraId = proposed.porraId();
            groupId = proposed.groupId();
            proposerId = proposed.proposerId();
            proposedResult = proposed.proposedResult();
            waitingEndsAt = proposed.waitingEndsAt();
            createdAt = proposed.occurredAt();
            proposalType = ResolutionProposalType.RESOLUTION;
            status = ResolutionStatus.WAITING;
        } else if (event instanceof CancellationProposed proposed) {
            id = proposed.resolutionId();
            porraId = proposed.porraId();
            groupId = proposed.groupId();
            proposerId = proposed.proposerId();
            proposedResult = CANCELLED_RESULT;
            waitingEndsAt = proposed.waitingEndsAt();
            createdAt = proposed.occurredAt();
            proposalType = ResolutionProposalType.CANCELLATION;
            proposalReason = proposed.reason();
            status = ResolutionStatus.WAITING;
        } else if (event instanceof ResolutionAutoAccepted accepted) {
            acceptedAt = accepted.occurredAt();
            acceptedResult = accepted.acceptedResult();
            acceptedMethod = ResolutionMethod.CONSENSUS;
            status = ResolutionStatus.ACCEPTED;
        } else if (event instanceof PorraCancelled cancelled) {
            acceptedAt = cancelled.occurredAt();
            acceptedResult = CANCELLED_RESULT;
            acceptedMethod = cancelled.method();
            status = ResolutionStatus.ACCEPTED;
        } else if (event instanceof ResolutionDisputed disputed) {
            disputerId = disputed.disputerId();
            disputeReason = disputed.reason();
            disputedAt = disputed.occurredAt();
            votingEndsAt = disputed.votingEndsAt();
            status = ResolutionStatus.VOTING;
        } else if (event instanceof VoteCast voteCast) {
            votes.put(voteCast.voterId(), new Vote(voteCast.voterId(), voteCast.votedResult(), voteCast.occurredAt()));
        } else if (event instanceof ResolutionAccepted accepted) {
            acceptedAt = accepted.occurredAt();
            acceptedResult = accepted.acceptedResult();
            acceptedMethod = accepted.method();
            status = ResolutionStatus.ACCEPTED;
        } else if (event instanceof CancellationRejected) {
            status = ResolutionStatus.REJECTED;
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    public static Resolution reconstitute(
            ResolutionId resolutionId,
            PorraId porraId,
            GroupId groupId,
            UserId proposerId,
            String proposedResult,
            Instant waitingEndsAt,
            ResolutionStatus status,
            Instant createdAt,
            Instant acceptedAt,
            UserId disputerId,
            String disputeReason,
            Instant disputedAt,
            Instant votingEndsAt,
            String acceptedResult,
            ResolutionMethod acceptedMethod,
            ResolutionProposalType proposalType,
            String proposalReason,
            List<Vote> existingVotes,
            long version) {
        Resolution resolution = new Resolution();
        resolution.id = resolutionId;
        resolution.porraId = porraId;
        resolution.groupId = groupId;
        resolution.proposerId = proposerId;
        resolution.proposedResult = proposedResult;
        resolution.waitingEndsAt = waitingEndsAt;
        resolution.status = status;
        resolution.createdAt = createdAt;
        resolution.acceptedAt = acceptedAt;
        resolution.disputerId = disputerId;
        resolution.disputeReason = disputeReason;
        resolution.disputedAt = disputedAt;
        resolution.votingEndsAt = votingEndsAt;
        resolution.acceptedResult = acceptedResult;
        resolution.acceptedMethod = acceptedMethod;
        resolution.proposalType = proposalType;
        resolution.proposalReason = proposalReason;
        existingVotes.forEach(vote -> resolution.votes.put(vote.voterId(), vote));
        resolution.rehydrateToVersion(version);
        return resolution;
    }

    public ResolutionId id() {
        return id;
    }

    public PorraId porraId() {
        return porraId;
    }

    public UserId proposerId() {
        return proposerId;
    }

    public String proposedResult() {
        return proposedResult;
    }

    public Instant waitingEndsAt() {
        return waitingEndsAt;
    }

    public ResolutionStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }

    public UserId disputerId() {
        return disputerId;
    }

    public String disputeReason() {
        return disputeReason;
    }

    public Instant disputedAt() {
        return disputedAt;
    }

    public Instant votingEndsAt() {
        return votingEndsAt;
    }

    public String acceptedResult() {
        return acceptedResult;
    }

    public ResolutionMethod acceptedMethod() {
        return acceptedMethod;
    }

    public List<Vote> votes() {
        return votes.values().stream()
                .sorted((left, right) -> left.votedAt().compareTo(right.votedAt()))
                .toList();
    }

    public ResolutionProposalType proposalType() {
        return proposalType;
    }

    public String proposalReason() {
        return proposalReason;
    }
}
