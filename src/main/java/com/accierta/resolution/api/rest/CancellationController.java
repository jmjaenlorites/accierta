package com.accierta.resolution.api.rest;

import com.accierta.resolution.api.dto.ProposeCancellationRequest;
import com.accierta.resolution.api.dto.ProposeCancellationResponse;
import com.accierta.resolution.application.query.ResolutionQueryService;
import com.accierta.resolution.application.query.ResolutionView;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.port.in.ProposeCancellationCommand;
import com.accierta.resolution.domain.port.in.ProposeCancellationUseCase;
import com.accierta.shared.security.JwtCurrentUserService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CancellationController {

    private final ProposeCancellationUseCase proposeCancellationUseCase;
    private final ResolutionQueryService resolutionQueryService;
    private final JwtCurrentUserService jwtCurrentUserService;

    public CancellationController(
            ProposeCancellationUseCase proposeCancellationUseCase,
            ResolutionQueryService resolutionQueryService,
            JwtCurrentUserService jwtCurrentUserService) {
        this.proposeCancellationUseCase = proposeCancellationUseCase;
        this.resolutionQueryService = resolutionQueryService;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @PostMapping("/api/porras/{porraId}/cancellation")
    @ResponseStatus(HttpStatus.CREATED)
    public ProposeCancellationResponse proposeCancellation(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId,
            @RequestBody(required = false) ProposeCancellationRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        ResolutionId resolutionId = proposeCancellationUseCase.propose(new ProposeCancellationCommand(
                porraId,
                currentUser.userId(),
                request == null ? null : request.reason()));
        ResolutionView resolution = resolutionQueryService.findByPorraId(porraId)
                .orElseThrow(() -> new java.util.NoSuchElementException("resolution not found"));
        return new ProposeCancellationResponse(
                resolutionId.value(),
                "CANCELLATION",
                resolution.waitingEndsAt(),
                ResolutionStatus.WAITING);
    }
}
