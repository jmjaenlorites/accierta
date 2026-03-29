package com.accierta.betting.application.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.Porra;
import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.betting.domain.port.in.CloseBettingCommand;
import com.accierta.betting.domain.port.out.PorraRepository;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CloseBettingHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T12:30:00Z"), ZoneOffset.UTC);

    @Mock
    private PorraRepository porraRepository;

    private CloseBettingHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CloseBettingHandler(porraRepository, FIXED_CLOCK);
    }

    @Test
    void should_close_overdue_porra_and_save_it() {
        PorraId porraId = PorraId.newId();
        Porra porra = Porra.create(
                porraId,
                GroupId.newId(),
                UserId.newId(),
                "Who wins?",
                null,
                PorraType.BINARY,
                null,
                null,
                50,
                Instant.parse("2026-03-26T11:00:00Z"),
                Instant.parse("2026-03-26T13:00:00Z"),
                BetVisibility.HIDDEN,
                true,
                Clock.fixed(Instant.parse("2026-03-26T10:00:00Z"), ZoneOffset.UTC));
        porra.pullUncommittedEvents();
        porra.placeBet(UserId.newId(), "true", Instant.parse("2026-03-26T10:15:00Z"));
        porra.placeBet(UserId.newId(), "false", Instant.parse("2026-03-26T10:20:00Z"));
        porra.pullUncommittedEvents();
        when(porraRepository.findById(porraId)).thenReturn(Optional.of(porra));

        handler.close(new CloseBettingCommand(porraId.value()));

        verify(porraRepository).save(porra);
    }
}
