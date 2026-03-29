package com.accierta.resolution.domain.port.out;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.resolution.domain.model.Resolution;
import com.accierta.resolution.domain.model.ResolutionId;
import java.util.Optional;

public interface ResolutionRepository {

    void save(Resolution resolution);

    Optional<Resolution> findByPorraId(PorraId porraId);

    Optional<Resolution> findById(ResolutionId resolutionId);
}
