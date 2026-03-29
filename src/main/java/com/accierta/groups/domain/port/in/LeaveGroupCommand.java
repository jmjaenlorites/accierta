package com.accierta.groups.domain.port.in;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public record LeaveGroupCommand(
        UUID groupId,
        UserId memberId) {
}
