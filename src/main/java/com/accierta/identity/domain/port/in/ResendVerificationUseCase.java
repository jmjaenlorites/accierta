package com.accierta.identity.domain.port.in;

public interface ResendVerificationUseCase {

    void resend(ResendVerificationCommand command);
}
