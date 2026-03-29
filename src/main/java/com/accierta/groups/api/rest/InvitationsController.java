package com.accierta.groups.api.rest;

import com.accierta.groups.api.dto.AcceptInvitationResponse;
import com.accierta.groups.api.dto.CreateInvitationRequest;
import com.accierta.groups.api.dto.CreateInvitationResponse;
import com.accierta.groups.application.query.InvitationInfoView;
import com.accierta.groups.domain.port.in.AcceptInvitationCommand;
import com.accierta.groups.domain.port.in.AcceptInvitationUseCase;
import com.accierta.groups.domain.port.in.CreateInvitationCommand;
import com.accierta.groups.domain.port.in.CreateInvitationUseCase;
import com.accierta.shared.security.JwtCurrentUserService;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
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
@RequestMapping
public class InvitationsController {

    private final CreateInvitationUseCase createInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final Function<String, java.util.Optional<InvitationInfoView>> invitationInfoQuery;
    private final JwtCurrentUserService jwtCurrentUserService;

    public InvitationsController(
            CreateInvitationUseCase createInvitationUseCase,
            AcceptInvitationUseCase acceptInvitationUseCase,
            Function<String, java.util.Optional<InvitationInfoView>> invitationInfoQuery,
            JwtCurrentUserService jwtCurrentUserService) {
        this.createInvitationUseCase = createInvitationUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.invitationInfoQuery = invitationInfoQuery;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @PostMapping("/api/groups/{groupId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateInvitationResponse createInvitation(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId,
            @RequestBody(required = false) CreateInvitationRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        var response = createInvitationUseCase.create(new CreateInvitationCommand(
                groupId,
                request == null ? null : request.email(),
                currentUser.userId()));
        return new CreateInvitationResponse(response.invitationToken(), response.invitationLink(), response.expiresAt());
    }

    @GetMapping("/api/invitations/{token}")
    public InvitationInfoView getInvitation(@PathVariable String token) {
        return invitationInfoQuery.apply(token)
                .orElseThrow(() -> new NoSuchElementException("invitation not found"));
    }

    @PostMapping("/api/invitations/{token}/accept")
    public AcceptInvitationResponse acceptInvitation(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String token) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        UUID groupId = acceptInvitationUseCase.accept(new AcceptInvitationCommand(token, currentUser.userId()));
        return new AcceptInvitationResponse(groupId, "Joined group successfully");
    }
}
