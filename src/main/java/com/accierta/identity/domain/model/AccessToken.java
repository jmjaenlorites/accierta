package com.accierta.identity.domain.model;

import java.time.Instant;

public record AccessToken(
        String value,
        Instant expiresAt) {
}
