package com.accierta.settlement.domain.port.in;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public record SettleDebtCommand(
        UUID debtId,
        UserId confirmerId) {
}
