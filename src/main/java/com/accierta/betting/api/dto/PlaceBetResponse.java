package com.accierta.betting.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record PlaceBetResponse(
        String message,
        UUID porraId,
        JsonNode prediction) {
}
