package com.accierta.settlement.domain.event;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.settlement.domain.model.DebtId;
import com.accierta.shared.domain.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;

public record DebtGenerated(
        DebtId debtId,
        GroupId groupId,
        PorraId porraId,
        UserId fromMemberId,
        UserId toMemberId,
        BigDecimal amount,
        String currency,
        Instant occurredAt) implements DomainEvent {
}
