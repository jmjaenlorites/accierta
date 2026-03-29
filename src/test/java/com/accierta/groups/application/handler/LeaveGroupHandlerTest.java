package com.accierta.groups.application.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.LeaveGroupCommand;
import com.accierta.groups.domain.port.out.ActivePorraChecker;
import com.accierta.groups.domain.port.out.GroupMembershipRepository;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LeaveGroupHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private GroupMembershipRepository groupMembershipRepository;
    @Mock private ActivePorraChecker activePorraChecker;

    private LeaveGroupHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new LeaveGroupHandler(groupMembershipRepository, activePorraChecker, FIXED_CLOCK);
    }

    @Test
    void should_leave_group_when_member_has_no_active_porras() {
        UUID groupId = UUID.randomUUID();
        UserId userId = UserId.newId();
        when(groupMembershipRepository.isActiveMember(new GroupId(groupId), new MemberId(userId))).thenReturn(true);
        when(activePorraChecker.findBlockingPorras(groupId, userId)).thenReturn(List.of());

        handler.leave(new LeaveGroupCommand(groupId, userId));

        verify(groupMembershipRepository).appendMemberLeftEvent(new GroupId(groupId), new MemberId(userId), 0, 0, FIXED_CLOCK.instant());
        verify(groupMembershipRepository).markLeft(new GroupId(groupId), new MemberId(userId), FIXED_CLOCK.instant());
    }

    @Test
    void should_reject_leave_when_member_has_active_porras() {
        UUID groupId = UUID.randomUUID();
        UserId userId = UserId.newId();
        when(groupMembershipRepository.isActiveMember(new GroupId(groupId), new MemberId(userId))).thenReturn(true);
        when(activePorraChecker.findBlockingPorras(groupId, userId))
                .thenReturn(List.of(new ActivePorraChecker.BlockingPorra(UUID.randomUUID(), "Liga", "PENDING_RESOLUTION")));

        assertThatThrownBy(() -> handler.leave(new LeaveGroupCommand(groupId, userId)))
                .isInstanceOf(LeaveGroupBlockedException.class)
                .hasMessage("member has active porras");
    }
}
