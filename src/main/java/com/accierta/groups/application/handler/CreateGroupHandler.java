package com.accierta.groups.application.handler;

import com.accierta.groups.domain.model.Currency;
import com.accierta.groups.domain.model.Group;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.model.GroupName;
import com.accierta.groups.domain.model.MemberId;
import com.accierta.groups.domain.port.in.CreateGroupCommand;
import com.accierta.groups.domain.port.in.CreateGroupUseCase;
import com.accierta.groups.domain.port.out.GroupRepository;
import com.accierta.groups.domain.port.out.VerifiedUserChecker;
import java.time.Clock;
import java.util.Objects;

public class CreateGroupHandler implements CreateGroupUseCase {

    private final GroupRepository groupRepository;
    private final VerifiedUserChecker verifiedUserChecker;
    private final Clock clock;

    public CreateGroupHandler(
            GroupRepository groupRepository,
            VerifiedUserChecker verifiedUserChecker,
            Clock clock) {
        this.groupRepository = Objects.requireNonNull(groupRepository, "groupRepository cannot be null");
        this.verifiedUserChecker = Objects.requireNonNull(verifiedUserChecker, "verifiedUserChecker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public GroupId create(CreateGroupCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        if (!verifiedUserChecker.isVerified(command.creatorId())) {
            throw new UnverifiedUserException("email not verified");
        }

        Currency currency = command.currency() == null || command.currency().isBlank()
                ? Currency.defaultCurrency()
                : new Currency(command.currency());
        Group group = Group.create(
                GroupId.newId(),
                new GroupName(command.name()),
                currency,
                new MemberId(command.creatorId()),
                clock);
        groupRepository.save(group);
        return group.id();
    }
}
