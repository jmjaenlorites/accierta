package com.accierta.identity.application.handler;

import com.accierta.identity.domain.model.RefreshToken;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.port.in.RefreshSessionCommand;
import com.accierta.identity.domain.port.in.RefreshSessionUseCase;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import com.accierta.identity.domain.port.out.TokenIssuer;
import com.accierta.identity.domain.port.out.TokenPair;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class RefreshSessionHandler implements RefreshSessionUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    public RefreshSessionHandler(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            TokenIssuer tokenIssuer,
            Clock clock) {
        this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository, "refreshTokenRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public TokenPair refresh(RefreshSessionCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        Instant now = Instant.now(clock);
        RefreshToken existingToken = refreshTokenRepository.findByValue(command.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("invalid or expired refresh token"));

        if (existingToken.invalidated()) {
            refreshTokenRepository.invalidateAllForUser(existingToken.userId(), now);
            throw new InvalidRefreshTokenException("invalid or expired refresh token");
        }
        if (existingToken.isExpiredAt(now)) {
            refreshTokenRepository.invalidate(existingToken.value(), now);
            throw new InvalidRefreshTokenException("invalid or expired refresh token");
        }

        User user = userRepository.findById(existingToken.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("invalid or expired refresh token"));
        var accessToken = tokenIssuer.issueAccessToken(user.id(), user.email(), now);
        var newRefreshToken = RefreshToken.issue(user.id(), now.plus(REFRESH_TOKEN_TTL));

        refreshTokenRepository.invalidate(existingToken.value(), now);
        refreshTokenRepository.save(newRefreshToken);

        return new TokenPair(
                accessToken.value(),
                newRefreshToken.value(),
                Duration.between(now, accessToken.expiresAt()).toSeconds());
    }
}
