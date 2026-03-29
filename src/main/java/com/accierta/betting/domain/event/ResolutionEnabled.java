package com.accierta.betting.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record ResolutionEnabled(
        PorraId porraId,
        GroupId groupId,
        Instant occurredAt) implements DomainEvent {
}
