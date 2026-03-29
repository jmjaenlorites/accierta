package com.accierta.identity.domain.port.in;

public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordCommand command);
}
