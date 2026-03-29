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
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2>Te han invitado a un grupo en Accierta</h2>
                  <p>Alguien te ha invitado a unirte a un grupo de apuestas en Accierta.</p>
                  <p style="text-align: center; margin: 24px 0;">
                    <a href="%s"
                       style="background: #6c5ce7; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: bold;">
                      Unirme al grupo
                    </a>
                  </p>
                  <p style="color: #71717a; font-size: 14px;">Esta invitación expira el %s.</p>
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
