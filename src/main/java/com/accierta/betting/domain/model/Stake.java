package com.accierta.betting.domain.model;

public record Stake(int value) {

    public Stake {
        if (value <= 0) {
            throw new IllegalArgumentException("stake must be greater than 0");
        }
    }
}
