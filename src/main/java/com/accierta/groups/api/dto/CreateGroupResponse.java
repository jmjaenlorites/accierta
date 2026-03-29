package com.accierta.groups.api.dto;

import java.util.UUID;

public record CreateGroupResponse(
        UUID groupId,
        String name,
        String currency) {
}
