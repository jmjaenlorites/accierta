package com.accierta.betting.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.List;

public record BettingDeadlineReached(
        PorraId porraId,
        GroupId groupId,
        List<RevealedBet> bets,
        Instant occurredAt) implements DomainEvent {

    public record RevealedBet(
            UserId memberId,
            String prediction,
            Instant placedAt) {
    }
}
