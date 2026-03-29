package com.accierta.identity.domain.port.in;

import com.accierta.identity.domain.model.UserId;

public interface RegisterUserUseCase {

    UserId register(RegisterUserCommand command);
}
