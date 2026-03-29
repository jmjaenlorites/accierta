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
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                  <h2 style="margin: 0 0 16px;">Restablecer contraseña</h2>
                  <p style="margin: 0 0 24px; color: #3f3f46;">Has solicitado restablecer tu contraseña en Accierta.</p>
                  <table width="100%%" cellpadding="0" cellspacing="0" role="presentation" style="margin: 0 0 24px;">
                    <tr>
                      <td align="center">
                        <a href="%s" style="display: inline-block; background: #6c5ce7; color: #ffffff; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px;">Restablecer contraseña</a>
                      </td>
                    </tr>
                  </table>
                  <p style="margin: 0; color: #a1a1aa; font-size: 13px;">Si no solicitaste este cambio, ignora este email. El enlace expira en 1 hora.</p>
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
