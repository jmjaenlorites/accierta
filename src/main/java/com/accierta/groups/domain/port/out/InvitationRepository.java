package com.accierta.groups.domain.port.out;

import com.accierta.groups.application.query.InvitationInfoView;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository {

    boolean isMember(GroupId groupId, MemberId memberId);

    boolean existsInvitationForMember(GroupId groupId, String email);

    void createInvitation(
            GroupId groupId,
            String invitedEmail,
            String invitationToken,
            Instant expiresAt,
            MemberId invitedBy,
            boolean emailSent,
            Instant occurredAt);

    Optional<StoredInvitation> findByToken(String invitationToken);

    void markUsed(String invitationToken, MemberId userId, Instant usedAt);

    Optional<InvitationInfoView> getInfo(String invitationToken, Instant now);

    boolean groupExists(UUID groupId);

    record StoredInvitation(
            UUID groupId,
            String invitedEmail,
            String invitationToken,
            Instant expiresAt,
            UUID invitedBy,
            boolean used) {
    }
}
