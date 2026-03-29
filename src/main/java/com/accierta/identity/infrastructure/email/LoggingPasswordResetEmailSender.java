package com.accierta.identity.infrastructure.email;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.ResetToken;
import com.accierta.identity.domain.port.out.PasswordResetEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingPasswordResetEmailSender implements PasswordResetEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPasswordResetEmailSender.class);

    @Override
    public void sendPasswordResetEmail(Email email, ResetToken resetToken) {
        LOGGER.info(
                "Sending password reset email to {} with token {} expiring at {}",
                email.value(),
                resetToken.value(),
                resetToken.expiresAt());
    }
}
