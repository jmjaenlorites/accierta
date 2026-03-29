package com.accierta.groups.api.dto;

import java.time.Instant;

public record CreateInvitationResponse(
        String invitationToken,
        String invitationLink,
        Instant expiresAt) {
}
