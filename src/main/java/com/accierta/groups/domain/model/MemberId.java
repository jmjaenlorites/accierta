package com.accierta.groups.domain.model;

import com.accierta.identity.domain.model.UserId;
import java.util.Objects;

public record MemberId(UserId value) {

    public MemberId {
        Objects.requireNonNull(value, "memberId cannot be null");
    }
}
