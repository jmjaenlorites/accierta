package com.accierta.betting.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.Porra;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.betting.domain.port.in.CreatePorraCommand;
import com.accierta.betting.domain.port.out.GroupMemberChecker;
import com.accierta.betting.domain.port.out.PorraRepository;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CreatePorraHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private PorraRepository porraRepository;
    @Mock private GroupMemberChecker groupMemberChecker;

    private CreatePorraHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CreatePorraHandler(porraRepository, groupMemberChecker, FIXED_CLOCK);
    }

    @Test
    void should_create_porra_for_group_member() {
        UUID groupId = UUID.randomUUID();
        UserId creatorId = UserId.newId();
        when(groupMemberChecker.isActiveMember(groupId, creatorId)).thenReturn(true);

        var porraId = handler.create(new CreatePorraCommand(
                groupId,
                creatorId,
                "Porra",
                null,
                PorraType.BINARY,
                null,
                null,
                10,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true));

        ArgumentCaptor<Porra> captor = ArgumentCaptor.forClass(Porra.class);
        verify(porraRepository).save(captor.capture());
        assertThat(porraId).isEqualTo(captor.getValue().id());
    }

    @Test
    void should_reject_non_member() {
        UUID groupId = UUID.randomUUID();
        UserId creatorId = UserId.newId();
        when(groupMemberChecker.isActiveMember(groupId, creatorId)).thenReturn(false);

        assertThatThrownBy(() -> handler.create(new CreatePorraCommand(
                groupId,
                creatorId,
                "Porra",
                null,
                PorraType.BINARY,
                null,
                null,
                10,
                FIXED_CLOCK.instant().plusSeconds(3600),
                FIXED_CLOCK.instant().plusSeconds(7200),
                BetVisibility.HIDDEN,
                true)))
                .isInstanceOf(PorraForbiddenException.class)
                .hasMessage("not a member");
    }
}
