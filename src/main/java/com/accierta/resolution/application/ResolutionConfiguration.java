package com.accierta.resolution.application;

import com.accierta.resolution.application.handler.AutoAcceptResolutionHandler;
import com.accierta.resolution.application.handler.CastVoteHandler;
import com.accierta.resolution.application.handler.DisputeResolutionHandler;
import com.accierta.resolution.application.handler.ProposeCancellationHandler;
import com.accierta.resolution.application.handler.ProposeResolutionHandler;
import com.accierta.resolution.application.handler.TallyVotesHandler;
import com.accierta.resolution.domain.port.in.AutoAcceptResolutionUseCase;
import com.accierta.resolution.domain.port.in.CastVoteUseCase;
import com.accierta.resolution.domain.port.in.DisputeResolutionUseCase;
import com.accierta.resolution.domain.port.in.ProposeCancellationUseCase;
import com.accierta.resolution.domain.port.in.ProposeResolutionUseCase;
import com.accierta.resolution.domain.port.in.TallyVotesUseCase;
import com.accierta.resolution.domain.port.out.CancelledPorraMarker;
import com.accierta.resolution.domain.port.out.PorraBetParticipantChecker;
import com.accierta.resolution.domain.port.out.PorraResolutionDetailsProvider;
import com.accierta.resolution.domain.port.out.ResolvedPorraMarker;
import com.accierta.resolution.domain.port.out.ResolutionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResolutionConfiguration {

    @Bean
    ProposeResolutionUseCase proposeResolutionUseCase(
            ResolutionRepository resolutionRepository,
            PorraResolutionDetailsProvider porraResolutionDetailsProvider,
            PorraBetParticipantChecker porraBetParticipantChecker,
            Clock clock) {
        return new ProposeResolutionHandler(
                resolutionRepository,
                porraResolutionDetailsProvider,
                porraBetParticipantChecker,
                clock);
    }

    @Bean
    ProposeCancellationUseCase proposeCancellationUseCase(
            ResolutionRepository resolutionRepository,
            PorraResolutionDetailsProvider porraResolutionDetailsProvider,
            PorraBetParticipantChecker porraBetParticipantChecker,
            Clock clock) {
        return new ProposeCancellationHandler(
                resolutionRepository,
                porraResolutionDetailsProvider,
                porraBetParticipantChecker,
                clock);
    }

    @Bean
    DisputeResolutionUseCase disputeResolutionUseCase(
            ResolutionRepository resolutionRepository,
            PorraBetParticipantChecker porraBetParticipantChecker,
            Clock clock) {
        return new DisputeResolutionHandler(
                resolutionRepository,
                porraBetParticipantChecker,
                clock);
    }

    @Bean
    CastVoteUseCase castVoteUseCase(
            ResolutionRepository resolutionRepository,
            PorraResolutionDetailsProvider porraResolutionDetailsProvider,
            PorraBetParticipantChecker porraBetParticipantChecker,
            Clock clock) {
        return new CastVoteHandler(
                resolutionRepository,
                porraResolutionDetailsProvider,
                porraBetParticipantChecker,
                clock);
    }

    @Bean
    AutoAcceptResolutionUseCase autoAcceptResolutionUseCase(
            ResolutionRepository resolutionRepository,
            CancelledPorraMarker cancelledPorraMarker,
            ResolvedPorraMarker resolvedPorraMarker,
            Clock clock) {
        return new AutoAcceptResolutionHandler(resolutionRepository, cancelledPorraMarker, resolvedPorraMarker, clock);
    }

    @Bean
    TallyVotesUseCase tallyVotesUseCase(
            ResolutionRepository resolutionRepository,
            CancelledPorraMarker cancelledPorraMarker,
            ResolvedPorraMarker resolvedPorraMarker,
            Clock clock) {
        return new TallyVotesHandler(
                resolutionRepository,
                cancelledPorraMarker,
                resolvedPorraMarker,
                clock);
    }
}
