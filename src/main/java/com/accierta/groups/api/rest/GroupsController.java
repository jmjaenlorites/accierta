package com.accierta.groups.api.rest;

import com.accierta.groups.api.dto.CreateGroupRequest;
import com.accierta.groups.api.dto.CreateGroupResponse;
import com.accierta.groups.api.dto.LeaveGroupResponse;
import com.accierta.groups.application.query.CanLeaveGroupQueryService;
import com.accierta.groups.application.query.CanLeaveGroupView;
import com.accierta.groups.application.query.GroupDetailView;
import com.accierta.groups.application.query.GroupAccessDeniedException;
import com.accierta.groups.application.query.GroupQueryService;
import com.accierta.groups.application.query.GroupSummaryView;
import com.accierta.groups.domain.port.in.LeaveGroupCommand;
import com.accierta.groups.domain.port.in.LeaveGroupUseCase;
import com.accierta.groups.domain.port.in.CreateGroupCommand;
import com.accierta.groups.domain.port.in.CreateGroupUseCase;
import com.accierta.shared.security.JwtCurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/groups")
public class GroupsController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GroupQueryService groupQueryService;
    private final CanLeaveGroupQueryService canLeaveGroupQueryService;
    private final LeaveGroupUseCase leaveGroupUseCase;
    private final JwtCurrentUserService jwtCurrentUserService;

    public GroupsController(
            CreateGroupUseCase createGroupUseCase,
            GroupQueryService groupQueryService,
            CanLeaveGroupQueryService canLeaveGroupQueryService,
            LeaveGroupUseCase leaveGroupUseCase,
            JwtCurrentUserService jwtCurrentUserService) {
        this.createGroupUseCase = createGroupUseCase;
        this.groupQueryService = groupQueryService;
        this.canLeaveGroupQueryService = canLeaveGroupQueryService;
        this.leaveGroupUseCase = leaveGroupUseCase;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGroupResponse create(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody CreateGroupRequest request) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        var groupId = createGroupUseCase.create(
                new CreateGroupCommand(request.name(), request.currency(), currentUser.userId()));
        return new CreateGroupResponse(
                groupId.value(),
                request.name().trim(),
                request.currency() == null || request.currency().isBlank() ? "\u20ac" : request.currency().trim());
    }

    @GetMapping
    public List<GroupSummaryView> list(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        return groupQueryService.listUserGroups(currentUser.userId());
    }

    @GetMapping("/{groupId}")
    public GroupDetailView getGroup(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        return groupQueryService.findGroup(groupId, currentUser.userId())
                .orElseGet(() -> {
                    if (groupQueryService.existsById(groupId)) {
                        throw new GroupAccessDeniedException("not a member");
                    }
                    throw new java.util.NoSuchElementException("group not found");
                });
    }

    @GetMapping("/{groupId}/can-leave")
    public CanLeaveGroupView canLeave(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        return canLeaveGroupQueryService.canLeave(groupId, currentUser.userId());
    }

    @PostMapping("/{groupId}/leave")
    public LeaveGroupResponse leave(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        leaveGroupUseCase.leave(new LeaveGroupCommand(groupId, currentUser.userId()));
        return new LeaveGroupResponse("Left group successfully");
    }
}
