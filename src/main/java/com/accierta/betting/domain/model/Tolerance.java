package com.accierta.betting.domain.model;

public record Tolerance(double value) {

    public Tolerance {
        if (value <= 0 || value > 1) {
            throw new IllegalArgumentException("tolerance must be > 0 and <= 1");
        }
    }

    public static Tolerance defaultTolerance() {
        return new Tolerance(0.15d);
    }
}
