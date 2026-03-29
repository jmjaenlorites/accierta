package com.accierta.betting.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PorraId(UUID value) {

    public PorraId {
        Objects.requireNonNull(value, "porraId cannot be null");
    }

    public static PorraId newId() {
        return new PorraId(UUID.randomUUID());
    }
}
