package com.accierta.betting.domain.port.in;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public record DeletePorraCommand(
        UUID porraId,
        UserId requesterId) {
}
