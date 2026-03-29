package com.accierta.betting.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record PorraCancelledByResolution(
        PorraId porraId,
        Instant occurredAt) implements DomainEvent {
}
