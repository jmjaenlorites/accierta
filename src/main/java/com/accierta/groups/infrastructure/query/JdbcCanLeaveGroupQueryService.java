package com.accierta.groups.infrastructure.query;

import com.accierta.groups.application.query.CanLeaveGroupQueryService;
import com.accierta.groups.application.query.CanLeaveGroupView;
import com.accierta.groups.domain.port.out.ActivePorraChecker;
import com.accierta.groups.domain.port.out.GroupMembershipRepository;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.identity.domain.model.UserId;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JdbcCanLeaveGroupQueryService implements CanLeaveGroupQueryService {

    private final GroupMembershipRepository groupMembershipRepository;
    private final ActivePorraChecker activePorraChecker;

    public JdbcCanLeaveGroupQueryService(
            GroupMembershipRepository groupMembershipRepository,
            ActivePorraChecker activePorraChecker) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.activePorraChecker = activePorraChecker;
    }

    @Override
    public CanLeaveGroupView canLeave(UUID groupId, UserId userId) {
        if (!groupMembershipRepository.isActiveMember(new GroupId(groupId), new MemberId(userId))) {
            return new CanLeaveGroupView(false, java.util.List.of());
        }
        var blocking = activePorraChecker.findBlockingPorras(groupId, userId).stream()
                .map(porra -> new CanLeaveGroupView.BlockingPorraView(porra.porraId(), porra.title(), porra.status()))
                .toList();
        return new CanLeaveGroupView(blocking.isEmpty(), blocking);
    }
}
