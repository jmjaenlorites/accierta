package com.accierta.resolution.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accierta.identity.domain.model.UserId;
import com.accierta.resolution.application.handler.ResolutionForbiddenException;
import com.accierta.resolution.application.query.ResolutionQueryService;
import com.accierta.resolution.application.query.ResolutionView;
import com.accierta.resolution.application.query.ResolutionVotesView;
import com.accierta.resolution.domain.model.ResolutionId;
import com.accierta.resolution.domain.model.ResolutionStatus;
import com.accierta.resolution.domain.port.in.CastVoteUseCase;
import com.accierta.resolution.domain.port.in.DisputeResolutionUseCase;
import com.accierta.resolution.domain.port.in.ProposeCancellationUseCase;
import com.accierta.resolution.domain.port.in.ProposeResolutionUseCase;
import com.accierta.shared.security.CurrentUser;
import com.accierta.shared.security.JwtCurrentUserService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ResolutionController.class, CancellationController.class})
class ResolutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProposeResolutionUseCase proposeResolutionUseCase;

    @MockBean
    private ProposeCancellationUseCase proposeCancellationUseCase;

    @MockBean
    private DisputeResolutionUseCase disputeResolutionUseCase;

    @MockBean
    private CastVoteUseCase castVoteUseCase;

    @MockBean
    private ResolutionQueryService resolutionQueryService;

    @MockBean
    private JwtCurrentUserService jwtCurrentUserService;

    @Test
    void should_propose_resolution() throws Exception {
        UUID porraId = UUID.randomUUID();
        UUID resolutionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(userId)));
        when(proposeResolutionUseCase.propose(any())).thenReturn(new ResolutionId(resolutionId));
        when(resolutionQueryService.findByPorraId(porraId)).thenReturn(Optional.of(
                new ResolutionView(
                        resolutionId,
                        "Barcelona",
                        new ResolutionView.ProposedByView(userId, "user@example.com"),
                        Instant.parse("2026-03-28T10:00:00Z"),
                        ResolutionStatus.WAITING,
                        Duration.ofHours(48))));

        mockMvc.perform(post("/api/porras/{porraId}/resolution", porraId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"proposedResult":"Barcelona"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resolutionId").value(resolutionId.toString()))
                .andExpect(jsonPath("$.proposedResult").value("Barcelona"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void should_return_forbidden_for_non_bettor() throws Exception {
        UUID porraId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(UserId.newId()));
        when(proposeResolutionUseCase.propose(any()))
                .thenThrow(new ResolutionForbiddenException("only bettors can propose"));

        mockMvc.perform(post("/api/porras/{porraId}/resolution", porraId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"proposedResult":"Barcelona"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("only bettors can propose"));
    }

    @Test
    void should_get_resolution_detail() throws Exception {
        UUID porraId = UUID.randomUUID();
        UUID resolutionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(userId)));
        when(resolutionQueryService.findByPorraId(porraId)).thenReturn(Optional.of(
                new ResolutionView(
                        resolutionId,
                        "Barcelona",
                        new ResolutionView.ProposedByView(userId, "user@example.com"),
                        Instant.parse("2026-03-28T10:00:00Z"),
                        ResolutionStatus.WAITING,
                        Duration.ofHours(12))));

        mockMvc.perform(get("/api/porras/{porraId}/resolution", porraId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolutionId").value(resolutionId.toString()))
                .andExpect(jsonPath("$.proposedBy.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void should_dispute_resolution() throws Exception {
        UUID porraId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(userId)));
        when(resolutionQueryService.findByPorraId(porraId)).thenReturn(Optional.of(
                new ResolutionView(
                        UUID.randomUUID(),
                        "Barcelona",
                        new ResolutionView.ProposedByView(userId, "user@example.com"),
                        Instant.parse("2026-03-28T10:00:00Z"),
                        ResolutionStatus.VOTING,
                        Duration.ofHours(48))));

        mockMvc.perform(post("/api/porras/{porraId}/resolution/dispute", porraId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"No estoy de acuerdo"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Resolution disputed, voting is now open"));
    }

    @Test
    void should_cast_vote() throws Exception {
        UUID porraId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(UserId.newId()));

        mockMvc.perform(post("/api/porras/{porraId}/resolution/vote", porraId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"votedResult":"Barcelona"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Vote cast"));
    }

    @Test
    void should_get_votes_view() throws Exception {
        UUID porraId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(UserId.newId()));
        when(resolutionQueryService.findVotesByPorraId(porraId)).thenReturn(Optional.of(
                new ResolutionVotesView(
                        ResolutionStatus.VOTING,
                        Instant.parse("2026-03-28T10:00:00Z"),
                        3,
                        2,
                        Duration.ofHours(2),
                        java.util.List.of(
                                new ResolutionVotesView.VoteView(UUID.randomUUID(), "A"),
                                new ResolutionVotesView.VoteView(UUID.randomUUID(), "B")))));

        mockMvc.perform(get("/api/porras/{porraId}/resolution/votes", porraId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VOTING"))
                .andExpect(jsonPath("$.votesCast").value(2));
    }

    @Test
    void should_propose_cancellation() throws Exception {
        UUID porraId = UUID.randomUUID();
        UUID resolutionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(userId)));
        when(proposeCancellationUseCase.propose(any())).thenReturn(new ResolutionId(resolutionId));
        when(resolutionQueryService.findByPorraId(porraId)).thenReturn(Optional.of(
                new ResolutionView(
                        resolutionId,
                        "CANCELLED",
                        new ResolutionView.ProposedByView(userId, "user@example.com"),
                        Instant.parse("2026-03-28T10:00:00Z"),
                        ResolutionStatus.WAITING,
                        Duration.ofHours(48))));

        mockMvc.perform(post("/api/porras/{porraId}/cancellation", porraId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Event suspended"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resolutionId").value(resolutionId.toString()))
                .andExpect(jsonPath("$.type").value("CANCELLATION"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}
