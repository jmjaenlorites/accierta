package com.accierta.groups.domain.port.out;

import java.time.Instant;
import java.util.UUID;

public interface InvitationEmailSender {

    void sendInvitationEmail(UUID groupId, String invitedEmail, String invitationToken, Instant expiresAt);
}
