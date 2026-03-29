package com.accierta.identity.infrastructure.email;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.VerificationToken;
import com.accierta.identity.domain.port.out.VerificationEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod & !staging")
public class LoggingVerificationEmailSender implements VerificationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingVerificationEmailSender.class);

    @Override
    public void sendVerificationEmail(Email email, VerificationToken verificationToken) {
        LOGGER.info(
                "Sending verification email to {} with token {} expiring at {}",
                email.value(),
                verificationToken.value(),
                verificationToken.expiresAt());
    }
}
