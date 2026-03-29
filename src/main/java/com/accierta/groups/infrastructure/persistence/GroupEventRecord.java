package com.accierta.groups.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public record GroupEventRecord(
        UUID aggregateId,
        long version,
        String eventType,
        String payload,
        Instant occurredAt) {
}
