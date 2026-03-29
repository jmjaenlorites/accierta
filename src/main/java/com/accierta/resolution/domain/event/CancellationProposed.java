package com.accierta.resolution.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record CancellationProposed(
        ResolutionId resolutionId,
        PorraId porraId,
        GroupId groupId,
        UserId proposerId,
        String reason,
        Instant waitingEndsAt,
        Instant occurredAt) implements DomainEvent {
}
