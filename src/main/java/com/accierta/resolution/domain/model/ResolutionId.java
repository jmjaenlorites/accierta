package com.accierta.resolution.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ResolutionId(UUID value) {

    public ResolutionId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static ResolutionId newId() {
        return new ResolutionId(UUID.randomUUID());
    }
}
