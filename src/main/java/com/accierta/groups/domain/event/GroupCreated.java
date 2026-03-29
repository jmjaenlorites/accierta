package com.accierta.groups.domain.event;

import com.accierta.groups.domain.model.Currency;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.GroupName;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record GroupCreated(
        GroupId groupId,
        GroupName groupName,
        Currency currency,
        MemberId creatorId,
        Instant occurredAt) implements DomainEvent {
}
