package com.accierta.betting.domain.port.out;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public interface GroupMemberChecker {

    boolean isActiveMember(UUID groupId, UserId userId);
}
