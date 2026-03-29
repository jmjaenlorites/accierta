package com.accierta.resolution.api.dto;

import java.time.Instant;

public record DisputeResolutionResponse(
        String message,
        Instant votingEndsAt) {
}
