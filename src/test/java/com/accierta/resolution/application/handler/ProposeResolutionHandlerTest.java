package com.accierta.resolution.application.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.domain.port.in.ProposeResolutionCommand;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import com.accierta.resolution.domain.port.out.PorraResolutionDetailsProvider;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProposeResolutionHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ResolutionRepository resolutionRepository;

    @Mock
    private PorraResolutionDetailsProvider porraDetailsProvider;

    @Mock
    private PorraBetParticipantChecker porraBetParticipantChecker;

    private ProposeResolutionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProposeResolutionHandler(
                resolutionRepository,
                porraDetailsProvider,
                porraBetParticipantChecker,
                FIXED_CLOCK);
    }

    @Test
    void should_save_resolution_when_porra_is_ready_and_user_is_bettor() {
        UUID porraId = UUID.randomUUID();
        UserId proposerId = UserId.newId();
        when(porraDetailsProvider.findById(porraId)).thenReturn(Optional.of(
                new PorraResolutionDetailsProvider.PorraResolutionDetails(
                        porraId,
                        UUID.randomUUID(),
                        PorraStatus.PENDING_RESOLUTION,
                        PorraType.MULTIPLE_CHOICE,
                        List.of("A", "B"))));
        when(porraBetParticipantChecker.hasBet(porraId, proposerId)).thenReturn(true);
        when(resolutionRepository.findByPorraId(any())).thenReturn(Optional.empty());

        handler.propose(new ProposeResolutionCommand(porraId, proposerId, "A"));

        verify(resolutionRepository).save(any());
    }

    @Test
    void should_reject_non_bettor() {
        UUID porraId = UUID.randomUUID();
        UserId proposerId = UserId.newId();
        when(porraDetailsProvider.findById(porraId)).thenReturn(Optional.of(
                new PorraResolutionDetailsProvider.PorraResolutionDetails(
                        porraId,
                        UUID.randomUUID(),
                        PorraStatus.PENDING_RESOLUTION,
                        PorraType.BINARY,
                        List.of())));
        when(porraBetParticipantChecker.hasBet(porraId, proposerId)).thenReturn(false);

        assertThatThrownBy(() -> handler.propose(new ProposeResolutionCommand(porraId, proposerId, "true")))
                .isInstanceOf(ResolutionForbiddenException.class)
                .hasMessage("only bettors can propose");
    }
}
