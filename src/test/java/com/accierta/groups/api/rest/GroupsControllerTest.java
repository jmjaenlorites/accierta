package com.accierta.groups.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accierta.groups.application.handler.UnverifiedUserException;
import com.accierta.groups.application.handler.LeaveGroupBlockedException;
import com.accierta.groups.application.query.CanLeaveGroupQueryService;
import com.accierta.groups.application.query.CanLeaveGroupView;
import com.accierta.groups.application.query.GroupDetailView;
import com.accierta.groups.application.query.GroupMemberView;
import com.accierta.groups.application.query.GroupQueryService;
import com.accierta.groups.application.query.GroupSummaryView;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.groups.domain.port.in.CreateGroupUseCase;
import com.accierta.groups.domain.port.in.LeaveGroupUseCase;
import com.accierta.identity.domain.model.UserId;
import com.accierta.shared.security.AuthenticationRequiredException;
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

@WebMvcTest(controllers = GroupsController.class)
class GroupsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CreateGroupUseCase createGroupUseCase;
    @MockBean private GroupQueryService groupQueryService;
    @MockBean private CanLeaveGroupQueryService canLeaveGroupQueryService;
    @MockBean private LeaveGroupUseCase leaveGroupUseCase;
    @MockBean private JwtCurrentUserService jwtCurrentUserService;

    @Test
    void should_create_group() throws Exception {
        UUID groupId = UUID.randomUUID();
        UserId userId = new UserId(UUID.randomUUID());
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(createGroupUseCase.create(any())).thenReturn(new GroupId(groupId));

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Fantasy Liga","currency":"🍺"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.currency").value("🍺"));
    }

    @Test
    void should_require_authentication() throws Exception {
        when(jwtCurrentUserService.requireUser(null))
                .thenThrow(new AuthenticationRequiredException("authentication required"));

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Fantasy Liga"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_reject_unverified_user() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(createGroupUseCase.create(any())).thenThrow(new UnverifiedUserException("email not verified"));

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Fantasy Liga"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("email not verified"));
    }

    @Test
    void should_list_user_groups() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(groupQueryService.listUserGroups(userId)).thenReturn(List.of(
                new GroupSummaryView(groupId, "Fantasy Liga", "🍺", 1, Instant.parse("2026-03-25T10:00:00Z"))));

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value(groupId.toString()))
                .andExpect(jsonPath("$[0].memberCount").value(1));
    }

    @Test
    void should_get_group_detail() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(groupQueryService.findGroup(groupId, userId)).thenReturn(Optional.of(
                new GroupDetailView(
                        groupId,
                        "Fantasy Liga",
                        "🍺",
                        List.of(new GroupMemberView(userId.value(), "user@example.com", Instant.parse("2026-03-25T10:00:00Z"))),
                        Instant.parse("2026-03-25T10:00:00Z"))));

        mockMvc.perform(get("/api/groups/{groupId}", groupId).header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.members[0].email").value("user@example.com"));
    }

    @Test
    void should_return_can_leave_view() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        when(canLeaveGroupQueryService.canLeave(groupId, userId))
                .thenReturn(new CanLeaveGroupView(true, List.of()));

        mockMvc.perform(get("/api/groups/{groupId}/can-leave", groupId).header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canLeave").value(true));
    }

    @Test
    void should_leave_group() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));

        mockMvc.perform(post("/api/groups/{groupId}/leave", groupId).header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Left group successfully"));
    }

    @Test
    void should_return_blocking_porras_when_leave_is_rejected() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());
        UUID groupId = UUID.randomUUID();
        when(jwtCurrentUserService.requireUser("Bearer token")).thenReturn(new CurrentUser(userId));
        doThrow(new LeaveGroupBlockedException(
                "member has active porras",
                new CanLeaveGroupView(false, List.of(
                        new CanLeaveGroupView.BlockingPorraView(UUID.randomUUID(), "Liga", "PENDING_RESOLUTION")))))
                .when(leaveGroupUseCase).leave(any());

        mockMvc.perform(post("/api/groups/{groupId}/leave", groupId).header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.canLeave").value(false))
                .andExpect(jsonPath("$.blockingPorras[0].status").value("PENDING_RESOLUTION"));
    }
}
