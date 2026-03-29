package com.accierta.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accierta.identity.domain.event.EmailVerified;
import com.accierta.identity.domain.event.PasswordReset;
import com.accierta.identity.domain.event.PasswordResetRequested;
import com.accierta.identity.domain.event.UserRegistered;
import com.accierta.identity.domain.event.VerificationEmailResent;
import com.accierta.shared.domain.DomainEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Test
    void should_emit_user_registered_when_registering_with_valid_data() {
        User user = User.register(
                UserId.newId(),
                new Email("Player@Example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                "invite-123",
                FIXED_CLOCK);

        List<DomainEvent> events = user.pullUncommittedEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(UserRegistered.class);
        UserRegistered event = (UserRegistered) events.getFirst();
        assertThat(event.email().value()).isEqualTo("player@example.com");
        assertThat(event.invitationToken()).isEqualTo("invite-123");
        assertThat(user.isVerified()).isFalse();
    }

    @Test
    void should_reject_registration_when_password_is_shorter_than_8_characters() {
        assertThatThrownBy(() -> new RawPassword("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must be at least 8 characters");
    }

    @Test
    void should_emit_email_verified_when_token_is_valid_and_not_expired() {
        User user = registeredUser();

        boolean verifiedNow = user.verifyEmail(user.verificationToken().value(), FIXED_CLOCK);
        List<DomainEvent> events = user.pullUncommittedEvents();

        assertThat(verifiedNow).isTrue();
        assertThat(events).hasSize(2);
        assertThat(events.get(1)).isInstanceOf(EmailVerified.class);
        assertThat(user.isVerified()).isTrue();
    }

    @Test
    void should_reject_verification_when_token_is_expired() {
        User user = registeredUser();
        Clock afterExpiry = Clock.fixed(user.verificationToken().expiresAt().plusSeconds(1), ZoneOffset.UTC);

        assertThatThrownBy(() -> user.verifyEmail(user.verificationToken().value(), afterExpiry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("verification token has expired");
    }

    @Test
    void should_succeed_idempotently_when_user_is_already_verified() {
        User user = registeredUser();
        user.verifyEmail(user.verificationToken().value(), FIXED_CLOCK);
        user.pullUncommittedEvents();

        boolean verifiedNow = user.verifyEmail(user.verificationToken().value(), FIXED_CLOCK);

        assertThat(verifiedNow).isFalse();
        assertThat(user.pullUncommittedEvents()).isEmpty();
    }

    @Test
    void should_generate_new_token_and_emit_event_when_resending_verification() {
        User user = registeredUser();
        String previousToken = user.verificationToken().value();

        VerificationToken newToken = user.resendVerification(FIXED_CLOCK);
        List<DomainEvent> events = user.pullUncommittedEvents();

        assertThat(newToken.value()).isNotEqualTo(previousToken);
        assertThat(events.getLast()).isInstanceOf(VerificationEmailResent.class);
        assertThat(user.verificationToken().value()).isEqualTo(newToken.value());
    }

    @Test
    void should_generate_reset_token_and_emit_event_when_requesting_password_reset() {
        User user = registeredUser();

        ResetToken resetToken = user.requestPasswordReset(FIXED_CLOCK);
        List<DomainEvent> events = user.pullUncommittedEvents();

        assertThat(resetToken.expiresAt()).isEqualTo(FIXED_CLOCK.instant().plusSeconds(3600));
        assertThat(events.getLast()).isInstanceOf(PasswordResetRequested.class);
        assertThat(user.resetToken()).isEqualTo(resetToken);
    }

    @Test
    void should_reset_password_and_clear_reset_token_when_token_is_valid() {
        User user = registeredUser();
        ResetToken resetToken = user.requestPasswordReset(FIXED_CLOCK);
        user.pullUncommittedEvents();

        user.resetPassword(
                resetToken.value(),
                new HashedPassword("$2a$10$new-hash"),
                FIXED_CLOCK);
        List<DomainEvent> events = user.pullUncommittedEvents();

        assertThat(events.getLast()).isInstanceOf(PasswordReset.class);
        assertThat(user.resetToken()).isNull();
        assertThat(user.hashedPassword()).isEqualTo(new HashedPassword("$2a$10$new-hash"));
    }

    @Test
    void should_reject_password_reset_when_token_is_expired() {
        User user = registeredUser();
        ResetToken resetToken = user.requestPasswordReset(FIXED_CLOCK);
        Clock afterExpiry = Clock.fixed(resetToken.expiresAt().plusSeconds(1), ZoneOffset.UTC);

        assertThatThrownBy(() -> user.resetPassword(
                resetToken.value(),
                new HashedPassword("$2a$10$new-hash"),
                afterExpiry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("reset token has expired");
    }

    private User registeredUser() {
        return User.register(
                UserId.newId(),
                new Email("user@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
    }
}
