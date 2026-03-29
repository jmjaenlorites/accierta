package com.accierta.groups.domain.port.in;

import com.accierta.groups.domain.model.GroupId;

public interface CreateGroupUseCase {

    GroupId create(CreateGroupCommand command);
}
