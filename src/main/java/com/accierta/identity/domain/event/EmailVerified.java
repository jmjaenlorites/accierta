package com.accierta.identity.domain.event;

import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record EmailVerified(
        UserId userId,
        Instant occurredAt) implements DomainEvent {
}
