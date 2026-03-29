package com.accierta.groups.application.query;

import com.accierta.identity.domain.model.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupQueryService {

    List<GroupSummaryView> listUserGroups(UserId userId);

    Optional<GroupDetailView> findGroup(UUID groupId, UserId userId);

    boolean existsById(UUID groupId);
}
