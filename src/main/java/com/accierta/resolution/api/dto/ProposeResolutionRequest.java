package com.accierta.resolution.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record ProposeResolutionRequest(JsonNode proposedResult) {
}
