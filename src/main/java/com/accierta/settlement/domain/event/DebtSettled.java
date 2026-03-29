package com.accierta.settlement.domain.event;

import com.accierta.identity.domain.model.UserId;
import com.accierta.settlement.domain.model.DebtId;
import com.accierta.shared.domain.DomainEvent;
import java.time.Instant;

public record DebtSettled(
        DebtId debtId,
        UserId confirmedBy,
        Instant occurredAt) implements DomainEvent {
}
