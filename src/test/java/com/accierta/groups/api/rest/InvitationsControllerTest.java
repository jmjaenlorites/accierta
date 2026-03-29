package com.accierta.groups.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accierta.groups.application.handler.InvitationAlreadyUsedException;
import com.accierta.groups.application.query.InvitationInfoView;
import com.accierta.groups.application.query.InvitationResponse;
import com.accierta.groups.domain.port.in.AcceptInvitationUseCase;
import com.accierta.groups.domain.port.in.CreateInvitationUseCase;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.security.AuthenticationRequiredException;
import com.accierta.shared.security.CurrentUser;
import com.accierta.shared.security.JwtCurrentUserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InvitationsController.class)
class InvitationsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CreateInvitationUseCase createInvitationUseCase;
    @MockBean private AcceptInvitationUseCase acceptInvitationUseCase;
    @MockBean private Function<String, Optional<InvitationInfoView>> invitationInfoQuery;
    @MockBean private JwtCurrentUserService jwtCurrentUserService;

    @Test
    void should_create_invitation() throws Exception {
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));
        when(createInvitationUseCase.create(any()))
                .thenReturn(new InvitationResponse("token-1", "https://accierta.app/invite/token-1", Instant.parse("2026-04-01T10:00:00Z")));

        mockMvc.perform(post("/api/groups/{groupId}/invitations", groupId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"friend@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationToken").value("token-1"));
    }

    @Test
    void should_return_invitation_info() throws Exception {
        when(invitationInfoQuery.apply("token-1"))
                .thenReturn(Optional.of(new InvitationInfoView("Fantasy Liga", "manual1@example.com", false, false)));

        mockMvc.perform(get("/api/invitations/token-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("Fantasy Liga"));
    }

    @Test
    void should_accept_invitation() throws Exception {
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));
        when(acceptInvitationUseCase.accept(any())).thenReturn(groupId);

        mockMvc.perform(post("/api/invitations/token-1/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.message").value("Joined group successfully"));
    }

    @Test
    void should_require_authentication_for_accept() throws Exception {
        when(jwtCurrentUserService.requireUser(null)).thenThrow(new AuthenticationRequiredException("authentication required"));

        mockMvc.perform(post("/api/invitations/token-1/accept"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_bad_request_for_used_invitation() throws Exception {
        when(jwtCurrentUserService.requireUser("Bearer token"))
                .thenReturn(new CurrentUser(new UserId(UUID.randomUUID())));
        when(acceptInvitationUseCase.accept(any())).thenThrow(new InvitationAlreadyUsedException("invitation already used"));

        mockMvc.perform(post("/api/invitations/token-1/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invitation already used"));
    }
}
