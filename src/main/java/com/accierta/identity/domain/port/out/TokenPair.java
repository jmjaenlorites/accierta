package com.accierta.identity.domain.port.out;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long expiresIn) {
}
