package com.accierta.groups.infrastructure.betting;

import com.accierta.groups.domain.port.out.ActivePorraChecker;
import com.accierta.identity.domain.model.UserId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NoActivePorraChecker implements ActivePorraChecker {

    @Override
    public List<BlockingPorra> findBlockingPorras(UUID groupId, UserId userId) {
        return List.of();
    }
}
