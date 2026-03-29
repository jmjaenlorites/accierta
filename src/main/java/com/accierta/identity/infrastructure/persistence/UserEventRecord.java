package com.accierta.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public record UserEventRecord(
        UUID aggregateId,
        long version,
        String eventType,
        String payload,
        Instant occurredAt) {
}
