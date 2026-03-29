package com.accierta.resolution.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record ResolutionDisputed(
        ResolutionId resolutionId,
        PorraId porraId,
        UserId disputerId,
        String reason,
        Instant votingEndsAt,
        Instant occurredAt) implements DomainEvent {
}
