package com.accierta.resolution.domain.port.out;

import com.accierta.betting.domain.model.PorraType;
import com.accierta.betting.domain.model.PorraStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PorraResolutionDetailsProvider {

    Optional<PorraResolutionDetails> findById(UUID porraId);

    record PorraResolutionDetails(
            UUID porraId,
            UUID groupId,
            PorraStatus status,
            PorraType type,
            List<String> options) {
    }
}
