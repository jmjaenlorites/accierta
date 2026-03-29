package com.accierta.identity.domain.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ResetToken(String value, Instant expiresAt) {

    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    public ResetToken {
        Objects.requireNonNull(value, "token value cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("token value cannot be blank");
        }
    }

    public static ResetToken create(Clock clock) {
        Instant now = Instant.now(clock);
        return new ResetToken(UUID.randomUUID().toString(), now.plus(TOKEN_TTL));
    }

    public boolean isExpiredAt(Instant instant) {
        return expiresAt.isBefore(instant);
    }
}
