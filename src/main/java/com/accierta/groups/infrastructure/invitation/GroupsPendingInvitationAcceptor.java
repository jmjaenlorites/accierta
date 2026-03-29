package com.accierta.groups.infrastructure.invitation;

import com.accierta.groups.domain.port.in.AcceptInvitationCommand;
import com.accierta.groups.domain.port.in.AcceptInvitationUseCase;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.out.PendingInvitationAcceptor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GroupsPendingInvitationAcceptor implements PendingInvitationAcceptor {

    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public GroupsPendingInvitationAcceptor(AcceptInvitationUseCase acceptInvitationUseCase) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
    }

    @Override
    public void acceptPendingInvitation(String invitationToken, UserId userId) {
        acceptInvitationUseCase.accept(new AcceptInvitationCommand(invitationToken, userId));
    }
}
