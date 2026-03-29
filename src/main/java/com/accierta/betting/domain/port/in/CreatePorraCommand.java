package com.accierta.betting.domain.port.in;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.identity.domain.model.UserId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreatePorraCommand(
        UUID groupId,
        UserId creatorId,
        String title,
        String description,
        PorraType type,
        List<String> options,
        Double tolerance,
        int stake,
        Instant bettingDeadline,
        Instant resolutionDate,
        BetVisibility visibility,
        Boolean betModifiable) {
}
