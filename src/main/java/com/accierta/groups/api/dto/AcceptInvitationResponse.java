package com.accierta.groups.api.dto;

import java.util.UUID;

public record AcceptInvitationResponse(
        UUID groupId,
        String message) {
}
