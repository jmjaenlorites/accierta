package com.accierta.settlement.domain.port.in;

public interface SettleDebtUseCase {

    void settle(SettleDebtCommand command);
}
