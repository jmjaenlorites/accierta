package com.accierta.resolution.domain.port.out;

import java.time.Instant;
import java.util.UUID;

public interface CancelledPorraMarker {

    void markCancelled(UUID porraId, Instant occurredAt);
}
