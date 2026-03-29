package com.accierta.settlement.application.handler;

import com.accierta.settlement.domain.model.DebtId;
import com.accierta.settlement.domain.port.in.SettleDebtCommand;
import com.accierta.settlement.domain.port.in.SettleDebtUseCase;
import com.accierta.settlement.domain.port.out.DebtRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class SettleDebtHandler implements SettleDebtUseCase {

    private final DebtRepository debtRepository;
    private final Clock clock;

    public SettleDebtHandler(DebtRepository debtRepository, Clock clock) {
        this.debtRepository = Objects.requireNonNull(debtRepository, "debtRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void settle(SettleDebtCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var debt = debtRepository.findById(new DebtId(command.debtId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("debt not found"));
        try {
            debt.settle(command.confirmerId(), Instant.now(clock));
        } catch (IllegalArgumentException exception) {
            throw new SettlementForbiddenException(exception.getMessage());
        }
        debtRepository.save(debt);
    }
}
