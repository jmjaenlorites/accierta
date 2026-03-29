package com.accierta.groups.domain.model;

import java.util.Objects;

public record Currency(String value) {

    private static final int MAX_LENGTH = 10;
    private static final String DEFAULT_VALUE = "EUR_SYMBOL";

    public Currency {
        Objects.requireNonNull(value, "currency cannot be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("currency cannot be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("currency must be at most 10 characters");
        }
        value = normalized;
    }

    public static Currency defaultCurrency() {
        return new Currency("\u20ac");
    }
}
