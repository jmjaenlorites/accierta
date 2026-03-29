package com.accierta.identity.application.handler;

import static org.mockito.Mockito.verify;

import com.accierta.identity.domain.port.in.LogoutCommand;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LogoutHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private RefreshTokenRepository refreshTokenRepository;

    private LogoutHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new LogoutHandler(refreshTokenRepository, FIXED_CLOCK);
    }

    @Test
    void should_invalidate_refresh_token_when_logging_out() {
        handler.logout(new LogoutCommand("refresh-token"));

        verify(refreshTokenRepository).invalidate("refresh-token", FIXED_CLOCK.instant());
    }
}
