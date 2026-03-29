package com.accierta.groups.domain.port.out;

import com.accierta.groups.application.query.CanLeaveGroupView;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import java.time.Instant;

public interface GroupMembershipRepository {

    boolean isActiveMember(GroupId groupId, MemberId memberId);

    void markLeft(GroupId groupId, MemberId memberId, Instant leftAt);

    void appendMemberLeftEvent(GroupId groupId, MemberId memberId, int pendingDebtsOwed, int pendingDebtsOwing, Instant occurredAt);
}
