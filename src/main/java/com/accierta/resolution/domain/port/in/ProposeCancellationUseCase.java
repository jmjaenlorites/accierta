package com.accierta.resolution.domain.port.in;

import com.accierta.resolution.domain.model.ResolutionId;

public interface ProposeCancellationUseCase {

    ResolutionId propose(ProposeCancellationCommand command);
}
