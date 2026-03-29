package com.accierta.betting.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record BetModified(
        PorraId porraId,
        UserId memberId,
        String oldPrediction,
        String newPrediction,
        Instant occurredAt) implements DomainEvent {
}
