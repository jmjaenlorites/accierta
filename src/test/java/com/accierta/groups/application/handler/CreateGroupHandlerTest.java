package com.accierta.groups.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.groups.domain.model.Group;
import com.accierta.groups.domain.port.in.CreateGroupCommand;
import com.accierta.groups.domain.port.out.GroupRepository;
import com.accierta.groups.domain.port.out.VerifiedUserChecker;
import com.accierta.identity.domain.model.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CreateGroupHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock private GroupRepository groupRepository;
    @Mock private VerifiedUserChecker verifiedUserChecker;

    private CreateGroupHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CreateGroupHandler(groupRepository, verifiedUserChecker, FIXED_CLOCK);
    }

    @Test
    void should_create_group_for_verified_user() {
        UserId creatorId = UserId.newId();
        when(verifiedUserChecker.isVerified(creatorId)).thenReturn(true);

        var groupId = handler.create(new CreateGroupCommand("Fantasy Liga", null, creatorId));

        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(captor.capture());
        assertThat(groupId).isEqualTo(captor.getValue().id());
        assertThat(captor.getValue().currency().value()).isEqualTo("\u20ac");
    }

    @Test
    void should_reject_unverified_user() {
        UserId creatorId = UserId.newId();
        when(verifiedUserChecker.isVerified(creatorId)).thenReturn(false);

        assertThatThrownBy(() -> handler.create(new CreateGroupCommand("Fantasy Liga", null, creatorId)))
                .isInstanceOf(UnverifiedUserException.class)
                .hasMessage("email not verified");
    }
}
