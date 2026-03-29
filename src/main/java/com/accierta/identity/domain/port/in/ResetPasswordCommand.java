package com.accierta.identity.domain.port.in;

public record ResetPasswordCommand(String token, String newPassword) {
}
