package com.accierta.resolution.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.resolution.domain.model.Resolution;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.port.in.ProposeResolutionCommand;
import com.accierta.resolution.domain.port.in.ProposeResolutionUseCase;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import com.accierta.resolution.domain.port.out.PorraResolutionDetailsProvider;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import com.accierta.resolution.domain.model.ResolutionStatus;

public class ProposeResolutionHandler implements ProposeResolutionUseCase {

    private final ResolutionRepository resolutionRepository;
    private final PorraResolutionDetailsProvider porraDetailsProvider;
    private final PorraBetParticipantChecker porraBetParticipantChecker;
    private final Clock clock;

    public ProposeResolutionHandler(
            ResolutionRepository resolutionRepository,
            PorraResolutionDetailsProvider porraDetailsProvider,
            PorraBetParticipantChecker porraBetParticipantChecker,
            Clock clock) {
        this.resolutionRepository = Objects.requireNonNull(resolutionRepository, "resolutionRepository cannot be null");
        this.porraDetailsProvider = Objects.requireNonNull(porraDetailsProvider, "porraDetailsProvider cannot be null");
        this.porraBetParticipantChecker = Objects.requireNonNull(
                porraBetParticipantChecker, "porraBetParticipantChecker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public ResolutionId propose(ProposeResolutionCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var porra = porraDetailsProvider.findById(command.porraId())
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found"));
        if (porra.status() != PorraStatus.PENDING_RESOLUTION) {
            throw new IllegalStateException("resolution not yet available");
        }
        if (!porraBetParticipantChecker.hasBet(command.porraId(), command.proposerId())) {
            throw new ResolutionForbiddenException("only bettors can propose");
        }
        var latest = resolutionRepository.findByPorraId(new PorraId(command.porraId()));
        if (latest.isPresent() && (latest.get().status() == ResolutionStatus.WAITING || latest.get().status() == ResolutionStatus.VOTING)) {
            throw new IllegalStateException("resolution already proposed");
        }
        Resolution resolution = Resolution.propose(
                ResolutionId.newId(),
                new PorraId(command.porraId()),
                new GroupId(porra.groupId()),
                command.proposerId(),
                porra.type(),
                porra.options(),
                command.proposedResult(),
                Instant.now(clock));
        resolutionRepository.save(resolution);
        return resolution.id();
    }
}
