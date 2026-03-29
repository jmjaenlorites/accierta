package com.accierta.betting.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.port.in.PlaceBetCommand;
import com.accierta.betting.domain.port.in.PlaceBetUseCase;
import com.accierta.betting.domain.port.out.BetCounter;
import com.accierta.betting.domain.port.out.GroupMemberChecker;
import com.accierta.betting.domain.port.out.PorraRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class PlaceBetHandler implements PlaceBetUseCase {

    private final PorraRepository porraRepository;
    private final GroupMemberChecker groupMemberChecker;
    private final BetCounter betCounter;
    private final Clock clock;

    public PlaceBetHandler(
            PorraRepository porraRepository,
            GroupMemberChecker groupMemberChecker,
            BetCounter betCounter,
            Clock clock) {
        this.porraRepository = Objects.requireNonNull(porraRepository, "porraRepository cannot be null");
        this.groupMemberChecker = Objects.requireNonNull(groupMemberChecker, "groupMemberChecker cannot be null");
        this.betCounter = Objects.requireNonNull(betCounter, "betCounter cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void place(PlaceBetCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var porra = porraRepository.findById(new PorraId(command.porraId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found"));
        if (!groupMemberChecker.isActiveMember(porra.groupId().value(), command.memberId())) {
            throw new PorraForbiddenException("not a member");
        }
        porra.placeBet(command.memberId(), command.prediction(), Instant.now(clock));
        porraRepository.save(porra);
    }
}
