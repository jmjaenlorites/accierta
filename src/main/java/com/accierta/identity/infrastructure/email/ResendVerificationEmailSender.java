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
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                  <h2 style="margin: 0 0 16px;">Verifica tu cuenta en Accierta</h2>
                  <p style="margin: 0 0 24px; color: #3f3f46;">Usa el botón de abajo para verificar tu cuenta, o copia el código manualmente en la app.</p>
                  <table width="100%%" cellpadding="0" cellspacing="0" role="presentation" style="margin: 0 0 24px;">
                    <tr>
                      <td align="center">
                        <a href="%s" style="display: inline-block; background: #6c5ce7; color: #ffffff; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px;">Verificar mi cuenta</a>
                      </td>
                    </tr>
                  </table>
                  <p style="margin: 0; color: #a1a1aa; font-size: 12px;">Si el botón no funciona, introduce este código en la app: <code style="color: #71717a;">%s</code></p>
                  <p style="margin: 8px 0 0; color: #a1a1aa; font-size: 12px;">Expira en 24 horas.</p>
                </div>
                """.formatted(verifyUrl, token);

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
