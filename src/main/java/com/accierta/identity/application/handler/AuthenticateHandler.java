package com.accierta.identity.application.handler;

import com.accierta.identity.domain.event.LoginFailed;
import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.RefreshToken;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.port.in.AuthenticateQuery;
import com.accierta.identity.domain.port.in.AuthenticateUseCase;
import com.accierta.identity.domain.port.out.AuthenticationFailureLogger;
import com.accierta.identity.domain.port.out.PasswordHasher;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import com.accierta.identity.domain.port.out.TokenIssuer;
import com.accierta.identity.domain.port.out.TokenPair;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class AuthenticateHandler implements AuthenticateUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationFailureLogger authenticationFailureLogger;
    private final Clock clock;

    public AuthenticateHandler(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationFailureLogger authenticationFailureLogger,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer cannot be null");
        this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository, "refreshTokenRepository cannot be null");
        this.authenticationFailureLogger =
                Objects.requireNonNull(authenticationFailureLogger, "authenticationFailureLogger cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public TokenPair authenticate(AuthenticateQuery query) {
        Objects.requireNonNull(query, "query cannot be null");

        Email email = new Email(query.email());
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logFailure(email, "unknown-email");
            throw new InvalidCredentialsException("invalid credentials");
        }

        RawPassword rawPassword = new RawPassword(query.password());
        if (!passwordHasher.matches(rawPassword, user.hashedPassword())) {
            logFailure(email, "wrong-password");
            throw new InvalidCredentialsException("invalid credentials");
        }

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException("email not verified");
        }

        Instant now = Instant.now(clock);
        var accessToken = tokenIssuer.issueAccessToken(user.id(), user.email(), now);
        var refreshToken = RefreshToken.issue(user.id(), now.plus(REFRESH_TOKEN_TTL));
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(
                accessToken.value(),
                refreshToken.value(),
                Duration.between(now, accessToken.expiresAt()).toSeconds());
    }

    private void logFailure(Email email, String reason) {
        authenticationFailureLogger.log(new LoginFailed(email, reason, Instant.now(clock)));
    }
}
