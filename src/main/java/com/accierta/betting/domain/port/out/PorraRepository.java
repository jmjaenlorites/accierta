package com.accierta.betting.domain.port.out;

import com.accierta.betting.domain.model.Porra;
import com.accierta.betting.domain.model.PorraId;
import java.util.Optional;

public interface PorraRepository {

    void save(Porra porra);

    Optional<Porra> findById(PorraId porraId);
}
