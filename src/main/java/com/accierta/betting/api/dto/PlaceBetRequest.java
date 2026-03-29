package com.accierta.betting.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record PlaceBetRequest(JsonNode prediction) {
}
