package com.accierta.groups.domain.port.in;

import java.util.UUID;

public interface AcceptInvitationUseCase {

    UUID accept(AcceptInvitationCommand command);
}
