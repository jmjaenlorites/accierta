package com.accierta.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RefreshToken(
        String value,
        UserId userId,
        Instant expiresAt,
        boolean invalidated) {

    public RefreshToken {
        Objects.requireNonNull(value, "refresh token value cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
    }

    public static RefreshToken issue(UserId userId, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID().toString(), userId, expiresAt, false);
    }

    public boolean isExpiredAt(Instant instant) {
        return expiresAt.isBefore(instant);
    }

    public RefreshToken invalidate() {
        return new RefreshToken(value, userId, expiresAt, true);
    }
}
