package com.accierta.groups.domain.event;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record MemberJoined(
        GroupId groupId,
        MemberId memberId,
        Instant occurredAt) implements DomainEvent {
}
