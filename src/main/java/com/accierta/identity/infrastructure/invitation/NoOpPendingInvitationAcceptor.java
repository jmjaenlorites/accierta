package com.accierta.identity.infrastructure.invitation;

import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.out.PendingInvitationAcceptor;
import org.springframework.stereotype.Component;

@Component
public class NoOpPendingInvitationAcceptor implements PendingInvitationAcceptor {

    @Override
    public void acceptPendingInvitation(String invitationToken, UserId userId) {
        // Replaced by the Groups adapter when that module is on the classpath.
    }
}
