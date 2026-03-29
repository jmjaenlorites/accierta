package com.accierta.resolution.application.handler;

import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.model.ResolutionProposalType;
import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.port.in.TallyVotesCommand;
import com.accierta.resolution.domain.port.in.TallyVotesUseCase;
import com.accierta.resolution.domain.port.out.CancelledPorraMarker;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import com.accierta.resolution.domain.port.out.ResolvedPorraMarker;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class TallyVotesHandler implements TallyVotesUseCase {

    private final ResolutionRepository resolutionRepository;
    private final CancelledPorraMarker cancelledPorraMarker;
    private final ResolvedPorraMarker resolvedPorraMarker;
    private final Clock clock;

    public TallyVotesHandler(
            ResolutionRepository resolutionRepository,
            CancelledPorraMarker cancelledPorraMarker,
            ResolvedPorraMarker resolvedPorraMarker,
            Clock clock) {
        this.resolutionRepository = Objects.requireNonNull(resolutionRepository, "resolutionRepository cannot be null");
        this.cancelledPorraMarker = Objects.requireNonNull(cancelledPorraMarker, "cancelledPorraMarker cannot be null");
        this.resolvedPorraMarker = Objects.requireNonNull(resolvedPorraMarker, "resolvedPorraMarker cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void tally(TallyVotesCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var resolution = resolutionRepository.findById(new ResolutionId(command.resolutionId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
        ResolutionStatus previousStatus = resolution.status();
        resolution.tallyVotes(Instant.now(clock));
        resolutionRepository.save(resolution);
        if (previousStatus != ResolutionStatus.ACCEPTED && resolution.status() == ResolutionStatus.ACCEPTED) {
            if (resolution.proposalType() == ResolutionProposalType.CANCELLATION) {
                cancelledPorraMarker.markCancelled(resolution.porraId().value(), resolution.acceptedAt());
            } else {
                resolvedPorraMarker.markResolved(resolution.porraId().value(), resolution.acceptedAt());
            }
        }
    }
}
