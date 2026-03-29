package com.accierta.groups.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.AcceptInvitationCommand;
import com.accierta.groups.domain.port.out.InvitationRepository;
import com.accierta.groups.domain.port.out.VerifiedUserChecker;
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

class AcceptInvitationHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private InvitationRepository invitationRepository;
    @Mock private VerifiedUserChecker verifiedUserChecker;

    private AcceptInvitationHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new AcceptInvitationHandler(invitationRepository, verifiedUserChecker, FIXED_CLOCK);
    }

    @Test
    void should_accept_valid_invitation_for_verified_non_member_user() {
        UUID groupId = UUID.randomUUID();
        UserId userId = UserId.newId();
        when(invitationRepository.findByToken("token-1"))
                .thenReturn(Optional.of(new InvitationRepository.StoredInvitation(
                        groupId,
                        null,
                        "token-1",
                        FIXED_CLOCK.instant().plusSeconds(60),
                        UUID.randomUUID(),
                        false)));
        when(verifiedUserChecker.isVerified(userId)).thenReturn(true);
        when(invitationRepository.isMember(new GroupId(groupId), new MemberId(userId))).thenReturn(false);

        UUID acceptedGroupId = handler.accept(new AcceptInvitationCommand("token-1", userId));

        assertThat(acceptedGroupId).isEqualTo(groupId);
        verify(invitationRepository).markUsed("token-1", new MemberId(userId), FIXED_CLOCK.instant());
    }

    @Test
    void should_reject_used_invitation() {
        UserId userId = UserId.newId();
        when(invitationRepository.findByToken("used-token"))
                .thenReturn(Optional.of(new InvitationRepository.StoredInvitation(
                        UUID.randomUUID(),
                        null,
                        "used-token",
                        FIXED_CLOCK.instant().plusSeconds(60),
                        UUID.randomUUID(),
                        true)));

        assertThatThrownBy(() -> handler.accept(new AcceptInvitationCommand("used-token", userId)))
                .isInstanceOf(InvitationAlreadyUsedException.class)
                .hasMessage("invitation already used");
    }
}
