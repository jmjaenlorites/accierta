package com.accierta.groups.domain.port.out;

import com.accierta.identity.domain.model.UserId;
import java.util.List;
import java.util.UUID;

public interface ActivePorraChecker {

    List<BlockingPorra> findBlockingPorras(UUID groupId, UserId userId);

    record BlockingPorra(
            UUID porraId,
            String title,
            String status) {
    }
}
