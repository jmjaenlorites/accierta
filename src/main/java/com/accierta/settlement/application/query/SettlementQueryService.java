package com.accierta.settlement.application.query;

import java.util.UUID;

public interface SettlementQueryService {

    PersonalDebtsView getMyDebts(UUID groupId, UUID memberId);

    LeaderboardView getLeaderboard(UUID groupId, LeaderboardRankBy rankBy);

    MemberStatsView getMemberStats(UUID groupId, UUID memberId);

    MemberHistoryPageView getMemberHistory(UUID groupId, UUID memberId, int page, int size);
}
