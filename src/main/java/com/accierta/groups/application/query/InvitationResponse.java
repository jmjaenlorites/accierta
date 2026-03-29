package com.accierta.groups.application.query;

import java.time.Instant;

public record InvitationResponse(
        String invitationToken,
        String invitationLink,
        Instant expiresAt) {
}
