package com.accierta.groups.domain.model;

import java.util.Objects;

public record GroupName(String value) {

    private static final int MAX_LENGTH = 100;

    public GroupName {
        Objects.requireNonNull(value, "group name cannot be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("group name cannot be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("group name must be at most 100 characters");
        }
        value = normalized;
    }
}
