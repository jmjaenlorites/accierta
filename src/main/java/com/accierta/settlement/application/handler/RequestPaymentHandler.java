package com.accierta.settlement.application.handler;

import com.accierta.settlement.domain.model.DebtId;
import com.accierta.settlement.domain.port.in.RequestPaymentCommand;
import com.accierta.settlement.domain.port.in.RequestPaymentUseCase;
import com.accierta.settlement.domain.port.out.DebtRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class RequestPaymentHandler implements RequestPaymentUseCase {

    private final DebtRepository debtRepository;
    private final Clock clock;

    public RequestPaymentHandler(DebtRepository debtRepository, Clock clock) {
        this.debtRepository = Objects.requireNonNull(debtRepository, "debtRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void request(RequestPaymentCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var debt = debtRepository.findById(new DebtId(command.debtId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("debt not found"));
        try {
            debt.requestPayment(command.requesterId(), Instant.now(clock));
        } catch (IllegalArgumentException exception) {
            throw new SettlementForbiddenException(exception.getMessage());
        }
        debtRepository.save(debt);
    }
}
