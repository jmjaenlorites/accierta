package com.accierta.settlement.domain.model;

import java.util.Objects;
import java.util.UUID;

public record DebtId(UUID value) {

    public DebtId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static DebtId newId() {
        return new DebtId(UUID.randomUUID());
    }
}
