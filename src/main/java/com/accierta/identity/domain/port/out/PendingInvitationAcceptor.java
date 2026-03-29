package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.UserId;

public interface PendingInvitationAcceptor {

    void acceptPendingInvitation(String invitationToken, UserId userId);
}
