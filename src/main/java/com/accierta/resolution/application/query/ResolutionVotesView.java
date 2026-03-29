package com.accierta.resolution.application.query;

import com.accierta.resolution.domain.model.ResolutionStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResolutionVotesView(
        ResolutionStatus status,
        Instant votingEndsAt,
        int eligibleVoters,
        int votesCast,
        Duration timeRemaining,
        List<VoteView> votes) {

    public record VoteView(
            UUID voterId,
            String votedResult) {
    }
}
