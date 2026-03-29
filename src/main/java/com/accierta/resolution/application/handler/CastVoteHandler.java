package com.accierta.resolution.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.resolution.domain.port.in.CastVoteCommand;
import com.accierta.resolution.domain.port.in.CastVoteUseCase;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import com.accierta.resolution.domain.port.out.PorraResolutionDetailsProvider;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CastVoteHandler implements CastVoteUseCase {

    private final ResolutionRepository resolutionRepository;
    private final PorraResolutionDetailsProvider porraDetailsProvider;
    private final PorraBetParticipantChecker porraBetParticipantChecker;
    private final Clock clock;

    public CastVoteHandler(
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
    public void cast(CastVoteCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var resolution = resolutionRepository.findByPorraId(new PorraId(command.porraId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
        if (!porraBetParticipantChecker.hasBet(command.porraId(), command.voterId())) {
            throw new ResolutionForbiddenException("not eligible to vote");
        }
        if (resolution.proposerId().equals(command.voterId())
                || (resolution.disputerId() != null && resolution.disputerId().equals(command.voterId()))) {
            throw new ResolutionForbiddenException("not eligible to vote");
        }
        var porra = porraDetailsProvider.findById(command.porraId())
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found"));
        resolution.castVote(
                command.voterId(),
                porra.type(),
                porra.options(),
                command.votedResult(),
                Instant.now(clock));
        resolutionRepository.save(resolution);
    }
}
