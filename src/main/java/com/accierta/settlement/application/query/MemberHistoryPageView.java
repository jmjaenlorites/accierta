package com.accierta.settlement.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MemberHistoryPageView(
        List<HistoryEntryView> content,
        int page,
        int totalPages) {

    public record HistoryEntryView(
            UUID porraId,
            String title,
            String type,
            String myBet,
            String result,
            String outcome,
            BigDecimal moneyChange,
            String currency,
            Instant resolvedAt) {
    }
}
