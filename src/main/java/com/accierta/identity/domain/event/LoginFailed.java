package com.accierta.identity.domain.event;

import com.accierta.identity.domain.model.Email;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record LoginFailed(
        Email email,
        String reason,
        Instant occurredAt) implements DomainEvent {
}
