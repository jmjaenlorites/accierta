package com.accierta.identity.application.handler;

import com.accierta.identity.domain.port.in.LogoutCommand;
import com.accierta.identity.domain.port.in.LogoutUseCase;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class LogoutHandler implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public LogoutHandler(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository, "refreshTokenRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void logout(LogoutCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        refreshTokenRepository.invalidate(command.refreshToken(), Instant.now(clock));
    }
}
