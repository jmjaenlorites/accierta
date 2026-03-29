package com.accierta.betting.api.rest;

import com.accierta.betting.api.dto.CreatePorraRequest;
import com.accierta.betting.api.dto.CreatePorraResponse;
import com.accierta.betting.api.dto.PlaceBetRequest;
import com.accierta.betting.api.dto.PlaceBetResponse;
import com.accierta.betting.application.query.PorraDetailView;
import com.accierta.betting.application.query.PorraQueryService;
import com.accierta.betting.application.query.PorraSummaryView;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.port.in.CreatePorraCommand;
import com.accierta.betting.domain.port.in.CreatePorraUseCase;
import com.accierta.betting.domain.port.in.DeletePorraCommand;
import com.accierta.betting.domain.port.in.DeletePorraUseCase;
import com.accierta.betting.domain.port.in.PlaceBetCommand;
import com.accierta.betting.domain.port.in.PlaceBetUseCase;
import com.accierta.shared.security.JwtCurrentUserService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class BettingController {

    private final CreatePorraUseCase createPorraUseCase;
    private final DeletePorraUseCase deletePorraUseCase;
    private final PlaceBetUseCase placeBetUseCase;
    private final PorraQueryService porraQueryService;
    private final JwtCurrentUserService jwtCurrentUserService;

    public BettingController(
            CreatePorraUseCase createPorraUseCase,
            DeletePorraUseCase deletePorraUseCase,
            PlaceBetUseCase placeBetUseCase,
            PorraQueryService porraQueryService,
            JwtCurrentUserService jwtCurrentUserService) {
        this.createPorraUseCase = createPorraUseCase;
        this.deletePorraUseCase = deletePorraUseCase;
        this.placeBetUseCase = placeBetUseCase;
        this.porraQueryService = porraQueryService;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @PostMapping("/api/groups/{groupId}/porras")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePorraResponse createPorra(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId,
            @RequestBody CreatePorraRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        var porraId = createPorraUseCase.create(new CreatePorraCommand(
                groupId,
                currentUser.userId(),
                request.title(),
                request.description(),
                request.type(),
                request.options(),
                request.tolerance(),
                request.stake(),
                request.bettingDeadline(),
                request.resolutionDate(),
                request.visibility(),
                request.betModifiable()));
        return new CreatePorraResponse(porraId.value(), request.title().trim(), request.type(), PorraStatus.OPEN);
    }

    @GetMapping("/api/groups/{groupId}/porras")
    public List<PorraSummaryView> listGroupPorras(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId,
            @RequestParam(required = false) PorraStatus status) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        return porraQueryService.listGroupPorras(groupId, status, currentUser.userId());
    }

    @GetMapping("/api/porras/{porraId}")
    public PorraDetailView getPorra(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        return porraQueryService.findPorra(porraId, currentUser.userId())
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found or not accessible"));
    }

    @DeleteMapping("/api/porras/{porraId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePorra(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        deletePorraUseCase.delete(new DeletePorraCommand(porraId, currentUser.userId()));
    }

    @PostMapping("/api/porras/{porraId}/bets")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceBetResponse placeBet(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID porraId,
            @RequestBody PlaceBetRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        String normalizedPrediction = request.prediction() == null
                ? "null"
                : request.prediction().isTextual()
                ? request.prediction().asText()
                : request.prediction().toString();
        placeBetUseCase.place(new PlaceBetCommand(porraId, currentUser.userId(), normalizedPrediction));
        return new PlaceBetResponse("Bet placed", porraId, request.prediction());
    }
}
