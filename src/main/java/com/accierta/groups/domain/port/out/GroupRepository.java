package com.accierta.groups.domain.port.out;

import com.accierta.groups.domain.model.Group;

public interface GroupRepository {

    void save(Group group);
}
