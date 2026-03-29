package com.accierta.identity.domain.port.in;

public interface RequestPasswordResetUseCase {

    void requestPasswordReset(RequestPasswordResetCommand command);
}
