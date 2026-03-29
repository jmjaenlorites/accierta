package com.accierta.resolution.domain.model;

import com.accierta.identity.domain.model.UserId;
import java.time.Instant;

public record Vote(
        UserId voterId,
        String votedResult,
        Instant votedAt) {
}
