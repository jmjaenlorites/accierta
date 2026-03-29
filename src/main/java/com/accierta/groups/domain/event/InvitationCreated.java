package com.accierta.groups.domain.event;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record InvitationCreated(
        GroupId groupId,
        String invitedEmail,
        String invitationToken,
        MemberId invitedBy,
        Instant occurredAt) implements DomainEvent {
}
