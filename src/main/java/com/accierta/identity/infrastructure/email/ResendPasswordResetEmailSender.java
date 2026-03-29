package com.accierta.identity.infrastructure.email;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.ResetToken;
import com.accierta.identity.domain.port.out.PasswordResetEmailSender;
import com.accierta.shared.infrastructure.email.AcciertaEmailProperties;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod", "staging"})
public class ResendPasswordResetEmailSender implements PasswordResetEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendPasswordResetEmailSender.class);

    private final Resend resend;
    private final AcciertaEmailProperties emailProperties;

    public ResendPasswordResetEmailSender(Resend resend, AcciertaEmailProperties emailProperties) {
        this.resend = resend;
        this.emailProperties = emailProperties;
    }

    @Override
    public void sendPasswordResetEmail(Email email, ResetToken resetToken) {
        String resetUrl = emailProperties.resetPasswordBaseUrl() + "?token=" + resetToken.value();

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2>Restablecer contraseña</h2>
                  <p>Has solicitado restablecer tu contraseña en Accierta.</p>
                  <p style="text-align: center; margin: 24px 0;">
                    <a href="%s"
                       style="background: #6c5ce7; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: bold;">
                      Restablecer contraseña
                    </a>
                  </p>
                  <p style="color: #71717a; font-size: 14px;">Si no solicitaste este cambio, ignora este email. El enlace expira en 1 hora.</p>
                </div>
                """.formatted(resetUrl);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(emailProperties.from())
                .to(email.value())
                .subject(emailProperties.subjectPrefix() + "Restablecer contraseña — Accierta")
                .html(html)
                .build();

        try {
            var response = resend.emails().send(params);
            LOGGER.info("Password reset email sent to {} (resend id: {})", email.value(), response.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to send password reset email to {}", email.value(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
