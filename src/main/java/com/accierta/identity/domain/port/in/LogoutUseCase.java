package com.accierta.identity.domain.port.in;

public interface LogoutUseCase {

    void logout(LogoutCommand command);
}
