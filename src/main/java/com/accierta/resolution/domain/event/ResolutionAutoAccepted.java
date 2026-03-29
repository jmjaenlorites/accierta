package com.accierta.resolution.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record ResolutionAutoAccepted(
        ResolutionId resolutionId,
        PorraId porraId,
        String acceptedResult,
        Instant occurredAt) implements DomainEvent {
}
