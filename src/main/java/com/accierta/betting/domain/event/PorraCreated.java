package com.accierta.betting.domain.event;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.List;

public record PorraCreated(
        PorraId porraId,
        GroupId groupId,
        UserId creatorId,
        String title,
        String description,
        PorraType type,
        List<String> options,
        Double tolerance,
        int stake,
        Instant bettingDeadline,
        Instant resolutionDate,
        BetVisibility visibility,
        boolean betModifiable,
        Instant occurredAt) implements DomainEvent {
}
