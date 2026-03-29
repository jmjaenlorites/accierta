package com.accierta.identity.domain.port.in;

public interface VerifyEmailUseCase {

    void verify(VerifyEmailCommand command);
}
