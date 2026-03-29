package com.accierta.resolution.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.resolution.domain.port.in.DisputeResolutionCommand;
import com.accierta.resolution.domain.port.in.DisputeResolutionUseCase;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class DisputeResolutionHandler implements DisputeResolutionUseCase {

    private final ResolutionRepository resolutionRepository;
    private final PorraBetParticipantChecker porraBetParticipantChecker;
    private final Clock clock;

    public DisputeResolutionHandler(
            ResolutionRepository resolutionRepository,
            PorraBetParticipantChecker porraBetParticipantChecker,
            Clock clock) {
        this.resolutionRepository = Objects.requireNonNull(resolutionRepository, "resolutionRepository cannot be null");
        this.porraBetParticipantChecker = Objects.requireNonNull(
                porraBetParticipantChecker, "porraBetParticipantChecker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void dispute(DisputeResolutionCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var resolution = resolutionRepository.findByPorraId(new PorraId(command.porraId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
        if (!porraBetParticipantChecker.hasBet(command.porraId(), command.disputerId())) {
            throw new ResolutionForbiddenException("only bettors can dispute");
        }
        if (resolution.proposerId().equals(command.disputerId())) {
            throw new ResolutionForbiddenException("proposer cannot dispute");
        }
        resolution.dispute(command.disputerId(), command.reason(), Instant.now(clock));
        resolutionRepository.save(resolution);
    }
}
