package com.accierta.groups.application.query;

import java.time.Instant;
import java.util.UUID;

public record GroupMemberView(
        UUID userId,
        String email,
        Instant joinedAt) {
}
