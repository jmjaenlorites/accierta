package com.accierta.groups.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InvitationToken(
        String value,
        Instant expiresAt) {

    private static final Duration TTL = Duration.ofDays(7);

    public InvitationToken {
        Objects.requireNonNull(value, "invitation token cannot be null");
        Objects.requireNonNull(expiresAt, "invitation expiry cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("invitation token cannot be blank");
        }
    }

    public static InvitationToken create(Instant now) {
        return new InvitationToken(UUID.randomUUID().toString(), now.plus(TTL));
    }

    public boolean isExpiredAt(Instant instant) {
        return expiresAt.isBefore(instant);
    }
}
