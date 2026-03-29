package com.accierta.resolution.api.rest;

import com.accierta.identity.api.dto.MessageResponse;
import com.accierta.resolution.api.dto.CastVoteRequest;
import com.accierta.resolution.api.dto.DisputeResolutionRequest;
import com.accierta.resolution.api.dto.DisputeResolutionResponse;
import com.accierta.resolution.api.dto.ProposeResolutionRequest;
import com.accierta.resolution.api.dto.ProposeResolutionResponse;
import com.accierta.resolution.application.query.ResolutionQueryService;
import com.accierta.resolution.application.query.ResolutionView;
import com.accierta.resolution.application.query.ResolutionVotesView;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.port.in.CastVoteCommand;
import com.accierta.resolution.domain.port.in.CastVoteUseCase;
import com.accierta.resolution.domain.port.in.DisputeResolutionCommand;
import com.accierta.resolution.domain.port.in.DisputeResolutionUseCase;
import com.accierta.resolution.domain.port.in.ProposeResolutionCommand;
import com.accierta.resolution.domain.port.in.ProposeResolutionUseCase;
import com.accierta.shared.security.JwtCurrentUserService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/porras/{porraId}/resolution")
public class ResolutionController {

    private final ProposeResolutionUseCase proposeResolutionUseCase;
    private final DisputeResolutionUseCase disputeResolutionUseCase;
    private final CastVoteUseCase castVoteUseCase;
    private final ResolutionQueryService resolutionQueryService;
    private final JwtCurrentUserService jwtCurrentUserService;

    public ResolutionController(
            ProposeResolutionUseCase proposeResolutionUseCase,
            DisputeResolutionUseCase disputeResolutionUseCase,
            CastVoteUseCase castVoteUseCase,
            ResolutionQueryService resolutionQueryService,
            JwtCurrentUserService jwtCurrentUserService) {
        this.proposeResolutionUseCase = proposeResolutionUseCase;
        this.disputeResolutionUseCase = disputeResolutionUseCase;
        this.castVoteUseCase = castVoteUseCase;
        this.resolutionQueryService = resolutionQueryService;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProposeResolutionResponse propose(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId,
            @RequestBody ProposeResolutionRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        String normalizedResult = request.proposedResult() == null
                ? "null"
                : request.proposedResult().isTextual()
                ? request.proposedResult().asText()
                : request.proposedResult().toString();
        ResolutionId resolutionId = proposeResolutionUseCase.propose(
                new ProposeResolutionCommand(porraId, currentUser.userId(), normalizedResult));
        ResolutionView resolution = resolutionQueryService.findByPorraId(porraId)
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
        return new ProposeResolutionResponse(
                resolutionId.value(),
                resolution.proposedResult(),
                currentUser.userId().value(),
                resolution.waitingEndsAt(),
                ResolutionStatus.WAITING);
    }

    @GetMapping
    public ResolutionView getResolution(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId) {
        jwtCurrentUserService.requireUser(authorizationHeader);
        return resolutionQueryService.findByPorraId(porraId)
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
    }

    @PostMapping("/dispute")
    @ResponseStatus(HttpStatus.CREATED)
    public DisputeResolutionResponse dispute(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId,
            @RequestBody(required = false) DisputeResolutionRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        disputeResolutionUseCase.dispute(new DisputeResolutionCommand(
                porraId,
                currentUser.userId(),
                request == null ? null : request.reason()));
        ResolutionView resolution = resolutionQueryService.findByPorraId(porraId)
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
        return new DisputeResolutionResponse("Resolution disputed, voting is now open", resolution.waitingEndsAt());
    }

    @PostMapping("/vote")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse castVote(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId,
            @RequestBody CastVoteRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        String normalizedVote = request.votedResult() == null
                ? "null"
                : request.votedResult().isTextual()
                ? request.votedResult().asText()
                : request.votedResult().toString();
        castVoteUseCase.cast(new CastVoteCommand(porraId, currentUser.userId(), normalizedVote));
        return new MessageResponse("Vote cast");
    }

    @GetMapping("/votes")
    public ResolutionVotesView getVotes(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId) {
        jwtCurrentUserService.requireUser(authorizationHeader);
        return resolutionQueryService.findVotesByPorraId(porraId)
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
    }
}
