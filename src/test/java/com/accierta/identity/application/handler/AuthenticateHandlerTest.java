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
import com.accierta.identity.domain.port.in.AuthenticateQuery;
import com.accierta.identity.domain.port.out.AuthenticationFailureLogger;
import com.accierta.identity.domain.port.out.PasswordHasher;
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

class AuthenticateHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private TokenIssuer tokenIssuer;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthenticationFailureLogger authenticationFailureLogger;

    private AuthenticateHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new AuthenticateHandler(
                userRepository,
                passwordHasher,
                tokenIssuer,
                refreshTokenRepository,
                authenticationFailureLogger,
                FIXED_CLOCK);
    }

    @Test
    void should_return_access_and_refresh_tokens_when_credentials_are_valid() {
        User user = verifiedUser();
        when(userRepository.findByEmail(new Email("user@example.com"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(new RawPassword("secure123"), user.hashedPassword())).thenReturn(true);
        when(tokenIssuer.issueAccessToken(user.id(), user.email(), FIXED_CLOCK.instant()))
                .thenReturn(new AccessToken("jwt-token", FIXED_CLOCK.instant().plusSeconds(900)));

        var result = handler.authenticate(new AuthenticateQuery("user@example.com", "secure123"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.expiresIn()).isEqualTo(900);
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(result.refreshToken()).isEqualTo(tokenCaptor.getValue().value());
    }

    @Test
    void should_reject_unknown_email_with_invalid_credentials() {
        when(userRepository.findByEmail(new Email("unknown@example.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.authenticate(new AuthenticateQuery("unknown@example.com", "secure123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("invalid credentials");

        verify(authenticationFailureLogger).log(any());
    }

    @Test
    void should_reject_wrong_password_with_invalid_credentials() {
        User user = verifiedUser();
        when(userRepository.findByEmail(new Email("user@example.com"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(new RawPassword("wrongpass"), user.hashedPassword())).thenReturn(false);

        assertThatThrownBy(() -> handler.authenticate(new AuthenticateQuery("user@example.com", "wrongpass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("invalid credentials");

        verify(authenticationFailureLogger).log(any());
    }

    @Test
    void should_reject_unverified_user() {
        User user = User.register(
                UserId.newId(),
                new Email("user@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        when(userRepository.findByEmail(new Email("user@example.com"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(new RawPassword("secure123"), user.hashedPassword())).thenReturn(true);

        assertThatThrownBy(() -> handler.authenticate(new AuthenticateQuery("user@example.com", "secure123")))
                .isInstanceOf(EmailNotVerifiedException.class)
                .hasMessage("email not verified");

        verify(refreshTokenRepository, never()).save(any());
    }

    private User verifiedUser() {
        User user = User.register(
                UserId.newId(),
                new Email("user@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        user.verifyEmail(user.verificationToken().value(), FIXED_CLOCK);
        user.pullUncommittedEvents();
        return user;
    }
}
