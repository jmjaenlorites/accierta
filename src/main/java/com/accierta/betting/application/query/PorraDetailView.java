package com.accierta.betting.application.query;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PorraDetailView(
        UUID porraId,
        UUID groupId,
        UUID creatorId,
        String title,
        String description,
        PorraType type,
        List<String> options,
        Double tolerance,
        int stake,
        Instant bettingDeadline,
        Instant resolutionDate,
        BetVisibility visibility,
        boolean betModifiable,
        PorraStatus status,
        List<BetView> bets,
        Instant createdAt) {

    public record BetView(
            UUID memberId,
            String prediction,
            Instant placedAt) {
    }
}
