package com.accierta.resolution.domain.port.in;

import java.util.UUID;

public record TallyVotesCommand(UUID resolutionId) {
}
