package com.accierta.betting.application.handler;

import com.accierta.betting.domain.model.Porra;
import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.port.in.CreatePorraCommand;
import com.accierta.betting.domain.port.in.CreatePorraUseCase;
import com.accierta.betting.domain.port.out.GroupMemberChecker;
import com.accierta.betting.domain.port.out.PorraRepository;
import com.accierta.groups.domain.model.GroupId;
import java.time.Clock;
import java.util.Objects;

public class CreatePorraHandler implements CreatePorraUseCase {

    private final PorraRepository porraRepository;
    private final GroupMemberChecker groupMemberChecker;
    private final Clock clock;

    public CreatePorraHandler(
            PorraRepository porraRepository,
            GroupMemberChecker groupMemberChecker,
            Clock clock) {
        this.porraRepository = Objects.requireNonNull(porraRepository, "porraRepository cannot be null");
        this.groupMemberChecker = Objects.requireNonNull(groupMemberChecker, "groupMemberChecker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public PorraId create(CreatePorraCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        if (!groupMemberChecker.isActiveMember(command.groupId(), command.creatorId())) {
            throw new PorraForbiddenException("not a member");
        }
        Porra porra = Porra.create(
                PorraId.newId(),
                new GroupId(command.groupId()),
                command.creatorId(),
                command.title(),
                command.description(),
                command.type(),
                command.options(),
                command.tolerance(),
                command.stake(),
                command.bettingDeadline(),
                command.resolutionDate(),
                command.visibility(),
                command.betModifiable(),
                clock);
        porraRepository.save(porra);
        return porra.id();
    }
}
