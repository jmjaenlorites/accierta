package com.accierta.groups.domain.port.in;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public record CreateInvitationCommand(
        UUID groupId,
        String invitedEmail,
        UserId inviterId) {
}
