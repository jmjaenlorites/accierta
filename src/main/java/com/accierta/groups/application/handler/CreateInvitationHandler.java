package com.accierta.groups.application.handler;

import com.accierta.groups.application.query.InvitationResponse;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.InvitationToken;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.CreateInvitationCommand;
import com.accierta.groups.domain.port.in.CreateInvitationUseCase;
import com.accierta.groups.domain.port.out.InvitationEmailSender;
import com.accierta.groups.domain.port.out.InvitationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreateInvitationHandler implements CreateInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final InvitationEmailSender invitationEmailSender;
    private final Clock clock;
    private final String invitationBaseUrl;

    public CreateInvitationHandler(
            InvitationRepository invitationRepository,
            InvitationEmailSender invitationEmailSender,
            Clock clock,
            String invitationBaseUrl) {
        this.invitationRepository = Objects.requireNonNull(invitationRepository, "invitationRepository cannot be null");
        this.invitationEmailSender = Objects.requireNonNull(invitationEmailSender, "invitationEmailSender cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
        this.invitationBaseUrl = Objects.requireNonNull(invitationBaseUrl, "invitationBaseUrl cannot be null");
    }

    @Override
    public InvitationResponse create(CreateInvitationCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        GroupId groupId = new GroupId(command.groupId());
        MemberId inviterId = new MemberId(command.inviterId());

        if (!invitationRepository.isMember(groupId, inviterId)) {
            throw new NotGroupMemberException("not a member");
        }

        String invitedEmail = command.invitedEmail() == null || command.invitedEmail().isBlank()
                ? null
                : command.invitedEmail().trim().toLowerCase();
        if (invitedEmail != null && invitationRepository.existsInvitationForMember(groupId, invitedEmail)) {
            throw new AlreadyGroupMemberException("user is already a member");
        }

        Instant now = Instant.now(clock);
        InvitationToken token = InvitationToken.create(now);
        boolean emailSent = invitedEmail != null;
        invitationRepository.createInvitation(
                groupId,
                invitedEmail,
                token.value(),
                token.expiresAt(),
                inviterId,
                emailSent,
                now);
        if (emailSent) {
            invitationEmailSender.sendInvitationEmail(groupId.value(), invitedEmail, token.value(), token.expiresAt());
        }
        return new InvitationResponse(token.value(), invitationBaseUrl + "/" + token.value(), token.expiresAt());
    }
}
