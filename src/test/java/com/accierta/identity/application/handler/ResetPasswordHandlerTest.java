package com.accierta.identity.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.RefreshToken;
import com.accierta.identity.domain.model.ResetToken;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.ResetPasswordCommand;
import com.accierta.identity.domain.port.out.PasswordHasher;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ResetPasswordHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private ResetPasswordHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ResetPasswordHandler(userRepository, passwordHasher, refreshTokenRepository, FIXED_CLOCK);
    }

    @Test
    void should_reset_password_and_invalidate_all_refresh_tokens_for_user() {
        User user = User.register(
                UserId.newId(),
                new Email("reset@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        ResetToken resetToken = user.requestPasswordReset(FIXED_CLOCK);
        user.pullUncommittedEvents();
        when(userRepository.findByResetToken(resetToken.value())).thenReturn(Optional.of(user));
        when(passwordHasher.hash(new RawPassword("newsecure123"))).thenReturn(new HashedPassword("$2a$10$new-hash"));

        handler.resetPassword(new ResetPasswordCommand(resetToken.value(), "newsecure123"));

        verify(userRepository).save(user);
        verify(refreshTokenRepository).invalidateAllForUser(user.id(), FIXED_CLOCK.instant());
        assertThat(user.hashedPassword()).isEqualTo(new HashedPassword("$2a$10$new-hash"));
        assertThat(user.resetToken()).isNull();
    }

    @Test
    void should_reject_reset_when_token_is_missing() {
        when(userRepository.findByResetToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.resetPassword(new ResetPasswordCommand("missing-token", "newsecure123")))
                .isInstanceOf(ResetTokenNotFoundException.class)
                .hasMessage("reset token is invalid");
    }

    @Test
    void should_reject_reset_when_password_is_shorter_than_8_characters() {
        User user = User.register(
                UserId.newId(),
                new Email("reset@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        ResetToken resetToken = user.requestPasswordReset(FIXED_CLOCK);
        user.pullUncommittedEvents();
        when(userRepository.findByResetToken(resetToken.value())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> handler.resetPassword(new ResetPasswordCommand(resetToken.value(), "short")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must be at least 8 characters");
    }

    @Test
    void should_reject_reset_when_token_is_expired() {
        User user = User.register(
                UserId.newId(),
                new Email("reset@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        ResetToken resetToken = user.requestPasswordReset(FIXED_CLOCK);
        user.pullUncommittedEvents();
        Clock afterExpiry = Clock.fixed(resetToken.expiresAt().plusSeconds(1), ZoneOffset.UTC);
        when(userRepository.findByResetToken(resetToken.value())).thenReturn(Optional.of(user));
        ResetPasswordHandler expiredHandler =
                new ResetPasswordHandler(userRepository, passwordHasher, refreshTokenRepository, afterExpiry);

        assertThatThrownBy(() -> expiredHandler.resetPassword(
                new ResetPasswordCommand(resetToken.value(), "newsecure123")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("reset token has expired");
    }
}
