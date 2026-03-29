package com.accierta.identity.domain.model;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "userId cannot be null");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }
}
