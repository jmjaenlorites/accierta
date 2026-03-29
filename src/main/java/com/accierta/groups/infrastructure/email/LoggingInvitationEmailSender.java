package com.accierta.groups.infrastructure.email;

import com.accierta.groups.domain.port.out.InvitationEmailSender;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingInvitationEmailSender implements InvitationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInvitationEmailSender.class);

    @Override
    public void sendInvitationEmail(UUID groupId, String invitedEmail, String invitationToken, Instant expiresAt) {
        LOGGER.info(
                "Sending invitation email for group {} to {} with token {} expiring at {}",
                groupId,
                invitedEmail,
                invitationToken,
                expiresAt);
    }
}
