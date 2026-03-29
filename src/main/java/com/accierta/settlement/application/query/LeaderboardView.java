package com.accierta.settlement.application.query;

import java.util.List;
import java.util.UUID;

public record LeaderboardView(
        LeaderboardRankBy rankBy,
        String currency,
        List<RankingEntryView> rankings) {

    public record RankingEntryView(
            int rank,
            UUID userId,
            String email,
            boolean left,
            MemberStatsView stats) {
    }
}
