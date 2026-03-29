package com.accierta.resolution.domain.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ResolvedPorraMarker {

    void markResolved(UUID porraId, Instant occurredAt);
}
