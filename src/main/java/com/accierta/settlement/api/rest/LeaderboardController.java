package com.accierta.settlement.api.rest;

import com.accierta.settlement.application.query.LeaderboardRankBy;
import com.accierta.settlement.application.query.LeaderboardView;
import com.accierta.settlement.application.query.MemberHistoryPageView;
import com.accierta.settlement.application.query.MemberStatsView;
import com.accierta.settlement.application.query.SettlementQueryService;
import com.accierta.shared.security.JwtCurrentUserService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}")
public class LeaderboardController {

    private final SettlementQueryService settlementQueryService;
    private final JwtCurrentUserService jwtCurrentUserService;

    public LeaderboardController(
            SettlementQueryService settlementQueryService,
            JwtCurrentUserService jwtCurrentUserService) {
        this.settlementQueryService = settlementQueryService;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @GetMapping("/leaderboard")
    public LeaderboardView leaderboard(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "NET_MONEY") LeaderboardRankBy rankBy) {
        jwtCurrentUserService.requireUser(authorizationHeader);
        return settlementQueryService.getLeaderboard(groupId, rankBy);
    }

    @GetMapping("/members/{userId}/stats")
    public MemberStatsView stats(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        jwtCurrentUserService.requireUser(authorizationHeader);
        return settlementQueryService.getMemberStats(groupId, userId);
    }

    @GetMapping("/members/{userId}/history")
    public MemberHistoryPageView history(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        jwtCurrentUserService.requireUser(authorizationHeader);
        return settlementQueryService.getMemberHistory(groupId, userId, page, size);
    }
}
