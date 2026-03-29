package com.accierta.groups.application.query;

import com.accierta.identity.domain.model.UserId;
import java.util.UUID;

public interface CanLeaveGroupQueryService {

    CanLeaveGroupView canLeave(UUID groupId, UserId userId);
}
