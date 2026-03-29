package com.accierta.betting.api.dto;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.PorraType;
import java.time.Instant;
import java.util.List;

public record CreatePorraRequest(
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
