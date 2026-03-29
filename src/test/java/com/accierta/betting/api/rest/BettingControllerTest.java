package com.accierta.betting.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accierta.betting.application.handler.PorraForbiddenException;
import com.accierta.betting.application.query.PorraDetailView;
import com.accierta.betting.application.query.PorraQueryService;
import com.accierta.betting.application.query.PorraSummaryView;
import com.accierta.betting.domain.model.BetVisibility;
import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.betting.domain.port.in.CreatePorraUseCase;
import com.accierta.betting.domain.port.in.DeletePorraUseCase;
import com.accierta.betting.domain.port.in.PlaceBetUseCase;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.security.CurrentUser;
import com.accierta.shared.security.JwtCurrentUserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BettingController.class)
class BettingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CreatePorraUseCase createPorraUseCase;
    @MockBean private DeletePorraUseCase deletePorraUseCase;
    @MockBean private PlaceBetUseCase placeBetUseCase;
    @MockBean private PorraQueryService porraQueryService;
    @MockBean private JwtCurrentUserService jwtCurrentUserService;

    @Test
    void should_create_porra() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID porraId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));
        when(createPorraUseCase.create(any())).thenReturn(new PorraId(porraId));

        mockMvc.perform(post("/api/groups/{groupId}/porras", groupId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Goals?",
                                  "type":"NUMERIC",
                                  "stake":50,
                                  "bettingDeadline":"2026-03-28T18:00:00Z",
                                  "resolutionDate":"2026-03-28T23:00:00Z",
                                  "visibility":"HIDDEN",
                                  "betModifiable":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.porraId").value(porraId.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void should_return_forbidden_for_non_member() throws Exception {
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));
        when(createPorraUseCase.create(any())).thenThrow(new PorraForbiddenException("not a member"));

        mockMvc.perform(post("/api/groups/{groupId}/porras", groupId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Goals?",
                                  "type":"BINARY",
                                  "stake":50,
                                  "bettingDeadline":"2026-03-28T18:00:00Z",
                                  "resolutionDate":"2026-03-28T23:00:00Z",
                                  "visibility":"HIDDEN"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("not a member"));
    }

    @Test
    void should_list_group_porras() throws Exception {
        UUID groupId = UUID.randomUUID();
        UserId userId = new UserId(UUID.randomUUID());
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(porraQueryService.listGroupPorras(groupId, PorraStatus.OPEN, userId)).thenReturn(List.of(
                new PorraSummaryView(UUID.randomUUID(), "Goals?", PorraType.NUMERIC, PorraStatus.OPEN, 50, Instant.parse("2026-03-28T18:00:00Z"), 0)));

        mockMvc.perform(get("/api/groups/{groupId}/porras", groupId)
                        .header("Authorization", "Bearer token")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void should_get_porra_detail() throws Exception {
        UUID porraId = UUID.randomUUID();
        UserId userId = new UserId(UUID.randomUUID());
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(porraQueryService.findPorra(porraId, userId)).thenReturn(Optional.of(
                new PorraDetailView(
                        porraId,
                        UUID.randomUUID(),
                        userId.value(),
                        "Goals?",
                        null,
                        PorraType.NUMERIC,
                        List.of(),
                        0.15d,
                        50,
                        Instant.parse("2026-03-28T18:00:00Z"),
                        Instant.parse("2026-03-28T23:00:00Z"),
                        BetVisibility.HIDDEN,
                        true,
                        PorraStatus.OPEN,
                        List.of(),
                        Instant.parse("2026-03-26T10:00:00Z"))));

        mockMvc.perform(get("/api/porras/{porraId}", porraId).header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porraId").value(porraId.toString()));
    }

    @Test
    void should_delete_porra() throws Exception {
        UUID porraId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));

        mockMvc.perform(delete("/api/porras/{porraId}", porraId).header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_place_bet() throws Exception {
        UUID porraId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));

        mockMvc.perform(post("/api/porras/{porraId}/bets", porraId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prediction": true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Bet placed"))
                .andExpect(jsonPath("$.porraId").value(porraId.toString()));
    }
}
