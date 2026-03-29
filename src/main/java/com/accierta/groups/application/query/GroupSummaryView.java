package com.accierta.groups.application.query;

import java.time.Instant;
import java.util.UUID;

public record GroupSummaryView(
        UUID groupId,
        String name,
        String currency,
        int memberCount,
        Instant createdAt) {
}
