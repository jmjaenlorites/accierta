package com.accierta.betting.domain.model;

import java.util.Objects;

public record Option(String value) {

    public Option {
        Objects.requireNonNull(value, "option cannot be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("option cannot be blank");
        }
        value = normalized;
    }
}
