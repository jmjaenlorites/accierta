package com.accierta.betting.application.query;

import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import java.time.Instant;
import java.util.UUID;

public record PorraSummaryView(
        UUID porraId,
        String title,
        PorraType type,
        PorraStatus status,
        int stake,
        Instant bettingDeadline,
        int totalBets) {
}
