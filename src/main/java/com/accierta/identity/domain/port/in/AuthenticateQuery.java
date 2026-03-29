package com.accierta.identity.domain.port.in;

public record AuthenticateQuery(
        String email,
        String password) {
}
