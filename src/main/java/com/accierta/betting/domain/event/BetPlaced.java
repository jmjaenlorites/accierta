package com.accierta.betting.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record BetPlaced(
        PorraId porraId,
        GroupId groupId,
        UserId memberId,
        String prediction,
        int stake,
        Instant occurredAt) implements DomainEvent {
}
