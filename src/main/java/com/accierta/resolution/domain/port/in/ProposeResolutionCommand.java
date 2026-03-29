package com.accierta.resolution.domain.port.in;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public record ProposeResolutionCommand(
        UUID porraId,
        UserId proposerId,
        String proposedResult) {
}
