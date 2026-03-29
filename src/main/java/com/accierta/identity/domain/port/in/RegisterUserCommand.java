package com.accierta.identity.domain.port.in;

public record RegisterUserCommand(
        String email,
        String password,
        String invitationToken) {
}
