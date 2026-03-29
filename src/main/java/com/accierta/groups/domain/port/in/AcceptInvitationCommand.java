package com.accierta.groups.domain.port.in;

import com.accierta.identity.domain.model.UserId;

public record AcceptInvitationCommand(
        String invitationToken,
        UserId userId) {
}
