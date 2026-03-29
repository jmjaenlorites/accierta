package com.accierta.identity.domain.model;

import java.util.Objects;

public record HashedPassword(String value) {

    public HashedPassword {
        Objects.requireNonNull(value, "hashedPassword cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("hashedPassword cannot be blank");
        }
    }
}
