package com.accierta.betting.application.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.Porra;
import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.betting.domain.port.in.PlaceBetCommand;
import com.accierta.betting.domain.port.out.BetCounter;
import com.accierta.betting.domain.port.out.GroupMemberChecker;
import com.accierta.betting.domain.port.out.PorraRepository;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PlaceBetHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private PorraRepository porraRepository;
    @Mock private GroupMemberChecker groupMemberChecker;
    @Mock private BetCounter betCounter;

    private PlaceBetHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new PlaceBetHandler(porraRepository, groupMemberChecker, betCounter, FIXED_CLOCK);
    }

    @Test
    void should_place_bet_for_active_member() {
        UUID groupId = UUID.randomUUID();
        UserId memberId = UserId.newId();
        Porra porra = Porra.create(
                PorraId.newId(),
                new GroupId(groupId),
                UserId.newId(),
                "Porra",
                null,
                PorraType.BINARY,
                null,
                null,
                10,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);
        porra.pullUncommittedEvents();
        when(porraRepository.findById(porra.id())).thenReturn(Optional.of(porra));
        when(groupMemberChecker.isActiveMember(groupId, memberId)).thenReturn(true);

        handler.place(new PlaceBetCommand(porra.id().value(), memberId, "true"));

        verify(porraRepository).save(porra);
    }

    @Test
    void should_reject_non_member_bet() {
        UUID groupId = UUID.randomUUID();
        UserId memberId = UserId.newId();
        Porra porra = Porra.create(
                PorraId.newId(),
                new GroupId(groupId),
                UserId.newId(),
                "Porra",
                null,
                PorraType.BINARY,
                null,
                null,
                10,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true,
                FIXED_CLOCK);
        porra.pullUncommittedEvents();
        when(porraRepository.findById(porra.id())).thenReturn(Optional.of(porra));
        when(groupMemberChecker.isActiveMember(groupId, memberId)).thenReturn(false);

        assertThatThrownBy(() -> handler.place(new PlaceBetCommand(porra.id().value(), memberId, "true")))
                .isInstanceOf(PorraForbiddenException.class)
                .hasMessage("not a member");
    }
}
