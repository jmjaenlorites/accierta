package com.accierta.resolution.domain.port.in;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public record DisputeResolutionCommand(
        UUID porraId,
        UserId disputerId,
        String reason) {
}
