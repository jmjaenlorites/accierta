package com.accierta.betting.domain.port.in;

import com.accierta.betting.domain.model.PorraId;

public interface CreatePorraUseCase {

    PorraId create(CreatePorraCommand command);
}
