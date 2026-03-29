package com.accierta.groups.application;

import com.accierta.groups.application.handler.CreateGroupHandler;
import com.accierta.groups.application.handler.AcceptInvitationHandler;
import com.accierta.groups.application.handler.CreateInvitationHandler;
import com.accierta.groups.application.handler.LeaveGroupHandler;
import com.accierta.groups.application.query.CanLeaveGroupQueryService;
import com.accierta.groups.application.query.InvitationInfoView;
import com.accierta.groups.domain.port.in.CreateGroupUseCase;
import com.accierta.groups.domain.port.in.AcceptInvitationUseCase;
import com.accierta.groups.domain.port.in.CreateInvitationUseCase;
import com.accierta.groups.domain.port.in.LeaveGroupUseCase;
import com.accierta.groups.domain.port.out.ActivePorraChecker;
import com.accierta.groups.domain.port.out.GroupRepository;
import com.accierta.groups.domain.port.out.GroupMembershipRepository;
import com.accierta.groups.domain.port.out.InvitationEmailSender;
import com.accierta.groups.domain.port.out.InvitationRepository;
import com.accierta.groups.domain.port.out.VerifiedUserChecker;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class GroupsConfiguration {

    @Bean
    CreateGroupUseCase createGroupUseCase(
            GroupRepository groupRepository,
            VerifiedUserChecker verifiedUserChecker,
            Clock clock) {
        return new CreateGroupHandler(groupRepository, verifiedUserChecker, clock);
    }

    @Bean
    CreateInvitationUseCase createInvitationUseCase(
            InvitationRepository invitationRepository,
            InvitationEmailSender invitationEmailSender,
            Clock clock,
            @Value("${accierta.groups.invitation-base-url}") String invitationBaseUrl) {
        return new CreateInvitationHandler(invitationRepository, invitationEmailSender, clock, invitationBaseUrl);
    }

    @Bean
    AcceptInvitationUseCase acceptInvitationUseCase(
            InvitationRepository invitationRepository,
            VerifiedUserChecker verifiedUserChecker,
            Clock clock) {
        return new AcceptInvitationHandler(invitationRepository, verifiedUserChecker, clock);
    }

    @Bean
    java.util.function.Function<String, Optional<InvitationInfoView>> invitationInfoQuery(InvitationRepository invitationRepository, Clock clock) {
        return token -> invitationRepository.getInfo(token, Instant.now(clock));
    }

    @Bean
    LeaveGroupUseCase leaveGroupUseCase(
            GroupMembershipRepository groupMembershipRepository,
            ActivePorraChecker activePorraChecker,
            Clock clock) {
        return new LeaveGroupHandler(groupMembershipRepository, activePorraChecker, clock);
    }
}
