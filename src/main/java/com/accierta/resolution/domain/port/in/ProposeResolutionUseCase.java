package com.accierta.resolution.domain.port.in;

import com.accierta.resolution.domain.model.ResolutionId;

public interface ProposeResolutionUseCase {

    ResolutionId propose(ProposeResolutionCommand command);
}
