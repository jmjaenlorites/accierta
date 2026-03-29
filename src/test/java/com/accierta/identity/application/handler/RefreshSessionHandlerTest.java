package com.accierta.identity.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.identity.domain.model.AccessToken;
import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.RefreshToken;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.RefreshSessionCommand;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import com.accierta.identity.domain.port.out.TokenIssuer;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RefreshSessionHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private TokenIssuer tokenIssuer;

    private RefreshSessionHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RefreshSessionHandler(refreshTokenRepository, userRepository, tokenIssuer, FIXED_CLOCK);
    }

    @Test
    void should_rotate_refresh_token_when_existing_token_is_valid() {
        User user = verifiedUser();
        RefreshToken existingToken = RefreshToken.issue(user.id(), FIXED_CLOCK.instant().plusSeconds(3600));
        when(refreshTokenRepository.findByValue("token-1")).thenReturn(Optional.of(
                new RefreshToken("token-1", user.id(), existingToken.expiresAt(), false)));
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(tokenIssuer.issueAccessToken(user.id(), user.email(), FIXED_CLOCK.instant()))
                .thenReturn(new AccessToken("jwt-token", FIXED_CLOCK.instant().plusSeconds(900)));

        var result = handler.refresh(new RefreshSessionCommand("token-1"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        verify(refreshTokenRepository).invalidate("token-1", FIXED_CLOCK.instant());
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(result.refreshToken()).isEqualTo(captor.getValue().value());
    }

    @Test
    void should_invalidate_all_tokens_when_refresh_token_was_already_invalidated() {
        UserId userId = UserId.newId();
        when(refreshTokenRepository.findByValue("used-token"))
                .thenReturn(Optional.of(new RefreshToken("used-token", userId, FIXED_CLOCK.instant().plusSeconds(60), true)));

        assertThatThrownBy(() -> handler.refresh(new RefreshSessionCommand("used-token")))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("invalid or expired refresh token");

        verify(refreshTokenRepository).invalidateAllForUser(userId, FIXED_CLOCK.instant());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void should_reject_expired_refresh_token() {
        UserId userId = UserId.newId();
        when(refreshTokenRepository.findByValue("expired-token"))
                .thenReturn(Optional.of(new RefreshToken("expired-token", userId, FIXED_CLOCK.instant().minusSeconds(1), false)));

        assertThatThrownBy(() -> handler.refresh(new RefreshSessionCommand("expired-token")))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("invalid or expired refresh token");

        verify(refreshTokenRepository).invalidate("expired-token", FIXED_CLOCK.instant());
    }

    private User verifiedUser() {
        User user = User.register(
                UserId.newId(),
                new Email("refresh@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        user.verifyEmail(user.verificationToken().value(), FIXED_CLOCK);
        user.pullUncommittedEvents();
        return user;
    }
}
