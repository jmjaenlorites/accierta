package com.accierta.betting.domain.model;

import com.accierta.identity.domain.model.UserId;
import java.time.Instant;
import java.util.Objects;

public record Bet(
        UserId memberId,
        String prediction,
        Instant placedAt) {

    public Bet {
        Objects.requireNonNull(memberId, "memberId cannot be null");
        Objects.requireNonNull(prediction, "prediction cannot be null");
        Objects.requireNonNull(placedAt, "placedAt cannot be null");
    }
}
