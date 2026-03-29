package com.accierta.groups.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.CreateInvitationCommand;
import com.accierta.groups.domain.port.out.InvitationEmailSender;
import com.accierta.groups.domain.port.out.InvitationRepository;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CreateInvitationHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private InvitationRepository invitationRepository;
    @Mock private InvitationEmailSender invitationEmailSender;

    private CreateInvitationHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CreateInvitationHandler(
                invitationRepository,
                invitationEmailSender,
                FIXED_CLOCK,
                "https://accierta.app/invite");
    }

    @Test
    void should_create_email_invitation_for_group_member() {
        UUID groupId = UUID.randomUUID();
        UserId inviterId = UserId.newId();
        when(invitationRepository.isMember(new GroupId(groupId), new MemberId(inviterId))).thenReturn(true);
        when(invitationRepository.existsInvitationForMember(new GroupId(groupId), "friend@example.com")).thenReturn(false);

        var response = handler.create(new CreateInvitationCommand(groupId, "friend@example.com", inviterId));

        assertThat(response.invitationToken()).isNotBlank();
        assertThat(response.invitationLink()).contains(response.invitationToken());
        verify(invitationEmailSender).sendInvitationEmail(groupId, "friend@example.com", response.invitationToken(), response.expiresAt());
    }

    @Test
    void should_reject_non_member_inviter() {
        UUID groupId = UUID.randomUUID();
        UserId inviterId = UserId.newId();
        when(invitationRepository.isMember(new GroupId(groupId), new MemberId(inviterId))).thenReturn(false);

        assertThatThrownBy(() -> handler.create(new CreateInvitationCommand(groupId, null, inviterId)))
                .isInstanceOf(NotGroupMemberException.class)
                .hasMessage("not a member");
    }
}
