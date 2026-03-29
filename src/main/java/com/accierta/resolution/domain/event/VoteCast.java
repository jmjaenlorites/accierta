package com.accierta.resolution.domain.event;

import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record VoteCast(
        ResolutionId resolutionId,
        UserId voterId,
        String votedResult,
        Instant occurredAt) implements DomainEvent {
}
