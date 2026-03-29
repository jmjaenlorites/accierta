package com.accierta.groups.infrastructure.email;

import com.accierta.groups.domain.port.out.InvitationEmailSender;
import com.accierta.shared.infrastructure.email.AcciertaEmailProperties;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod", "staging"})
public class ResendInvitationEmailSender implements InvitationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendInvitationEmailSender.class);

    private final Resend resend;
    private final AcciertaEmailProperties emailProperties;
    private final String invitationBaseUrl;

    public ResendInvitationEmailSender(
            Resend resend,
            AcciertaEmailProperties emailProperties,
            @Value("${accierta.groups.invitation-base-url}") String invitationBaseUrl) {
        this.resend = resend;
        this.emailProperties = emailProperties;
        this.invitationBaseUrl = invitationBaseUrl;
    }

    @Override
    public void sendInvitationEmail(UUID groupId, String invitedEmail, String invitationToken, Instant expiresAt) {
        String inviteUrl = invitationBaseUrl + "/" + invitationToken;
        String expiresFormatted = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.of("Europe/Madrid"))
                .format(expiresAt);

        String html = """
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                  <h2 style="margin: 0 0 16px;">Te han invitado a un grupo en Accierta</h2>
                  <p style="margin: 0 0 24px; color: #3f3f46;">Alguien te ha invitado a unirte a un grupo de apuestas en Accierta.</p>
                  <table width="100%%" cellpadding="0" cellspacing="0" role="presentation" style="margin: 0 0 24px;">
                    <tr>
                      <td align="center">
                        <a href="%s" style="display: inline-block; background: #6c5ce7; color: #ffffff; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px;">Unirme al grupo</a>
                      </td>
                    </tr>
                  </table>
                  <p style="margin: 0; color: #a1a1aa; font-size: 13px;">Esta invitación expira el %s.</p>
                </div>
                """.formatted(inviteUrl, expiresFormatted);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(emailProperties.from())
                .to(invitedEmail)
                .subject(emailProperties.subjectPrefix() + "Te han invitado a un grupo en Accierta")
                .html(html)
                .build();

        try {
            var response = resend.emails().send(params);
            LOGGER.info("Invitation email sent to {} for group {} (resend id: {})", invitedEmail, groupId, response.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to send invitation email to {} for group {}", invitedEmail, groupId, e);
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }
}
