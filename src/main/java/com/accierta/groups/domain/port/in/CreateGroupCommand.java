package com.accierta.groups.domain.port.in;

import com.accierta.identity.domain.model.UserId;

public record CreateGroupCommand(
        String name,
        String currency,
        UserId creatorId) {
}
