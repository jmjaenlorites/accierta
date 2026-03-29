package com.accierta.groups.application.handler;

import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.AcceptInvitationCommand;
import com.accierta.groups.domain.port.in.AcceptInvitationUseCase;
import com.accierta.groups.domain.port.out.InvitationRepository;
import com.accierta.groups.domain.port.out.VerifiedUserChecker;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AcceptInvitationHandler implements AcceptInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final VerifiedUserChecker verifiedUserChecker;
    private final Clock clock;

    public AcceptInvitationHandler(
            InvitationRepository invitationRepository,
            VerifiedUserChecker verifiedUserChecker,
            Clock clock) {
        this.invitationRepository = Objects.requireNonNull(invitationRepository, "invitationRepository cannot be null");
        this.verifiedUserChecker = Objects.requireNonNull(verifiedUserChecker, "verifiedUserChecker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public UUID accept(AcceptInvitationCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        Instant now = Instant.now(clock);
        var invitation = invitationRepository.findByToken(command.invitationToken())
                .orElseThrow(() -> new InvitationInvalidException("invitation not found"));

        if (invitation.used()) {
            throw new InvitationAlreadyUsedException("invitation already used");
        }
        if (invitation.expiresAt().isBefore(now)) {
            throw new InvitationInvalidException("invitation expired");
        }
        if (!verifiedUserChecker.isVerified(command.userId())) {
            throw new UnverifiedUserException("email not verified");
        }

        GroupId groupId = new GroupId(invitation.groupId());
        MemberId userId = new MemberId(command.userId());
        if (invitationRepository.isMember(groupId, userId)) {
            throw new AlreadyGroupMemberException("already a member");
        }

        invitationRepository.markUsed(command.invitationToken(), userId, now);
        return invitation.groupId();
    }
}
