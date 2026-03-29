package com.accierta.identity.domain.port.in;

import com.accierta.identity.domain.port.out.TokenPair;

public interface RefreshSessionUseCase {

    TokenPair refresh(RefreshSessionCommand command);
}
