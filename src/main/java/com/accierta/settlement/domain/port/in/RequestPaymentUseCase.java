package com.accierta.settlement.domain.port.in;

public interface RequestPaymentUseCase {

    void request(RequestPaymentCommand command);
}
