package com.accierta.groups.application.handler;

import com.accierta.groups.application.query.CanLeaveGroupView;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.LeaveGroupCommand;
import com.accierta.groups.domain.port.in.LeaveGroupUseCase;
import com.accierta.groups.domain.port.out.ActivePorraChecker;
import com.accierta.groups.domain.port.out.GroupMembershipRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class LeaveGroupHandler implements LeaveGroupUseCase {

    private final GroupMembershipRepository groupMembershipRepository;
    private final ActivePorraChecker activePorraChecker;
    private final Clock clock;

    public LeaveGroupHandler(
            GroupMembershipRepository groupMembershipRepository,
            ActivePorraChecker activePorraChecker,
            Clock clock) {
        this.groupMembershipRepository = Objects.requireNonNull(groupMembershipRepository, "groupMembershipRepository cannot be null");
        this.activePorraChecker = Objects.requireNonNull(activePorraChecker, "activePorraChecker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void leave(LeaveGroupCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        GroupId groupId = new GroupId(command.groupId());
        MemberId memberId = new MemberId(command.memberId());
        if (!groupMembershipRepository.isActiveMember(groupId, memberId)) {
            throw new NotGroupMemberException("not a member");
        }

        var blockingPorras = activePorraChecker.findBlockingPorras(command.groupId(), command.memberId()).stream()
                .map(porra -> new CanLeaveGroupView.BlockingPorraView(porra.porraId(), porra.title(), porra.status()))
                .toList();
        if (!blockingPorras.isEmpty()) {
            throw new LeaveGroupBlockedException("member has active porras", new CanLeaveGroupView(false, blockingPorras));
        }

        Instant now = Instant.now(clock);
        groupMembershipRepository.appendMemberLeftEvent(groupId, memberId, 0, 0, now);
        groupMembershipRepository.markLeft(groupId, memberId, now);
    }
}
