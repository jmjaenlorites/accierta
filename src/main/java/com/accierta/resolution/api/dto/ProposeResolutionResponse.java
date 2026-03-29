package com.accierta.resolution.api.dto;

import com.accierta.resolution.domain.model.ResolutionStatus;
import java.time.Instant;
import java.util.UUID;

public record ProposeResolutionResponse(
        UUID resolutionId,
        String proposedResult,
        UUID proposedBy,
        Instant waitingEndsAt,
        ResolutionStatus status) {
}
