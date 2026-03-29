package com.accierta.resolution.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.resolution.domain.model.Resolution;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.port.in.ProposeCancellationCommand;
import com.accierta.resolution.domain.port.in.ProposeCancellationUseCase;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import com.accierta.resolution.domain.port.out.PorraResolutionDetailsProvider;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class ProposeCancellationHandler implements ProposeCancellationUseCase {

    private final ResolutionRepository resolutionRepository;
    private final PorraResolutionDetailsProvider porraDetailsProvider;
    private final PorraBetParticipantChecker porraBetParticipantChecker;
    private final Clock clock;

    public ProposeCancellationHandler(
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
    public ResolutionId propose(ProposeCancellationCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var porra = porraDetailsProvider.findById(command.porraId())
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found"));
        if (porra.status() != PorraStatus.CLOSED && porra.status() != PorraStatus.PENDING_RESOLUTION) {
            throw new IllegalStateException("porra still open");
        }
        if (!porraBetParticipantChecker.hasBet(command.porraId(), command.proposerId())) {
            throw new ResolutionForbiddenException("only bettors can propose cancellation");
        }
        var latest = resolutionRepository.findByPorraId(new PorraId(command.porraId()));
        if (latest.isPresent() && (latest.get().status() == ResolutionStatus.WAITING || latest.get().status() == ResolutionStatus.VOTING)) {
            throw new IllegalStateException("active proposal exists");
        }
        Resolution resolution = Resolution.proposeCancellation(
                ResolutionId.newId(),
                new PorraId(command.porraId()),
                new GroupId(porra.groupId()),
                command.proposerId(),
                command.reason(),
                Instant.now(clock));
        resolutionRepository.save(resolution);
        return resolution.id();
    }
}
