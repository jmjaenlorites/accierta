package com.accierta.groups.domain.event;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record InvitationSent(
        GroupId groupId,
        String invitedEmail,
        String invitationToken,
        Instant occurredAt) implements DomainEvent {
}
