package com.accierta.resolution.domain.port.out;

import com.accierta.identity.domain.model.UserId;
import java.util.List;
import java.util.UUID;

public interface PorraBettorsProvider {

    List<UserId> findBettors(UUID porraId);
}
