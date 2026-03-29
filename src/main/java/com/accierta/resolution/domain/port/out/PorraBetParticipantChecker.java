package com.accierta.resolution.domain.port.out;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public interface PorraBetParticipantChecker {

    boolean hasBet(UUID porraId, UserId userId);
}
