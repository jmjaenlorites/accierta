package com.accierta.groups.application.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupDetailView(
        UUID groupId,
        String name,
        String currency,
        List<GroupMemberView> members,
        Instant createdAt) {
}
