package com.accierta.identity.api.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn) {
}
