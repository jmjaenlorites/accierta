package com.accierta.groups.domain.model;

import java.util.Objects;
import java.util.UUID;

public record GroupId(UUID value) {

    public GroupId {
        Objects.requireNonNull(value, "groupId cannot be null");
    }

    public static GroupId newId() {
        return new GroupId(UUID.randomUUID());
    }
}
