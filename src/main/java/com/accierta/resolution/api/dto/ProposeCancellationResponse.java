package com.accierta.resolution.api.dto;

import com.accierta.resolution.domain.model.ResolutionStatus;
import java.time.Instant;
import java.util.UUID;

public record ProposeCancellationResponse(
        UUID resolutionId,
        String type,
        Instant waitingEndsAt,
        ResolutionStatus status) {
}
