package com.accierta.resolution.application.query;

import com.accierta.resolution.domain.model.ResolutionStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record ResolutionView(
        UUID resolutionId,
        String proposedResult,
        ProposedByView proposedBy,
        Instant waitingEndsAt,
        ResolutionStatus status,
        Duration timeRemaining) {

    public record ProposedByView(
            UUID userId,
            String email) {
    }
}
