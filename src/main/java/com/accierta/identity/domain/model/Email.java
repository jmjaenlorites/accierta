package com.accierta.identity.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final int MAX_LENGTH = 320;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public Email {
        Objects.requireNonNull(value, "email cannot be null");
        String normalized = value.trim().toLowerCase();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("email cannot be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("email is too long");
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("email format is invalid");
        }

        value = normalized;
    }
}
