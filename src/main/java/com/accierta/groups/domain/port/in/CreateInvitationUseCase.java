package com.accierta.groups.domain.port.in;

import com.accierta.groups.application.query.InvitationResponse;

public interface CreateInvitationUseCase {

    InvitationResponse create(CreateInvitationCommand command);
}
