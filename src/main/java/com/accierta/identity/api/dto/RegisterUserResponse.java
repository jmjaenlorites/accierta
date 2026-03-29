package com.accierta.identity.api.dto;

import java.util.UUID;

public record RegisterUserResponse(
        UUID userId,
        String message) {
}
