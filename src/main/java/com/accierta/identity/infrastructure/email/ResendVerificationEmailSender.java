package com.accierta.identity.infrastructure.email;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.VerificationToken;
import com.accierta.identity.domain.port.out.VerificationEmailSender;
import com.accierta.shared.infrastructure.email.AcciertaEmailProperties;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod", "staging"})
public class ResendVerificationEmailSender implements VerificationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendVerificationEmailSender.class);

    private final Resend resend;
    private final AcciertaEmailProperties emailProperties;

    public ResendVerificationEmailSender(Resend resend, AcciertaEmailProperties emailProperties) {
        this.resend = resend;
        this.emailProperties = emailProperties;
    }

    @Override
    public void sendVerificationEmail(Email email, VerificationToken verificationToken) {
        String token = verificationToken.value();
        String verifyUrl = emailProperties.verificationBaseUrl() + "?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2>Verifica tu cuenta en Accierta</h2>
                  <p>Tu código de verificación es:</p>
                  <div style="background: #f4f4f5; border-radius: 8px; padding: 16px; text-align: center; margin: 24px 0;">
                    <code style="font-size: 24px; letter-spacing: 2px; font-weight: bold;">%s</code>
                  </div>
                  <p>Copia este código y pégalo en la app, o haz clic en el siguiente enlace:</p>
                  <p><a href="%s">Verificar mi cuenta</a></p>
                  <p style="color: #71717a; font-size: 14px;">Este código expira en 24 horas.</p>
                </div>
                """.formatted(token, verifyUrl);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(emailProperties.from())
                .to(email.value())
                .subject(emailProperties.subjectPrefix() + "Verifica tu cuenta en Accierta")
                .html(html)
                .build();

        try {
            var response = resend.emails().send(params);
            LOGGER.info("Verification email sent to {} (resend id: {})", email.value(), response.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to send verification email to {}", email.value(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
