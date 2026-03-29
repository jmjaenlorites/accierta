package com.accierta.identity.domain.model;

import java.util.Objects;

public record RawPassword(String value) {

    private static final int MIN_LENGTH = 8;

    public RawPassword {
        Objects.requireNonNull(value, "password cannot be null");
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("password must be at least 8 characters");
        }
    }
}
