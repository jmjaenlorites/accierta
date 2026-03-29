package com.accierta.identity.domain.event;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record UserRegistered(
        UserId userId,
        Email email,
        String invitationToken,
        Instant occurredAt) implements DomainEvent {
}
