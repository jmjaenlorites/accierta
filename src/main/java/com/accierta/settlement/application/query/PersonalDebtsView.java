package com.accierta.settlement.application.query;

import com.accierta.settlement.domain.model.DebtStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PersonalDebtsView(
        List<OutgoingDebtView> iOwe,
        List<IncomingDebtView> owedToMe,
        List<ConsolidatedDebtView> consolidated) {

    public record MemberView(
            UUID userId,
            String email) {
    }

    public record OutgoingDebtView(
            UUID debtId,
            UUID porraId,
            String porraTitle,
            MemberView toMember,
            BigDecimal amount,
            String currency,
            DebtStatus status,
            Instant createdAt) {
    }

    public record IncomingDebtView(
            UUID debtId,
            UUID porraId,
            String porraTitle,
            MemberView fromMember,
            BigDecimal amount,
            String currency,
            DebtStatus status,
            Instant createdAt) {
    }

    public record ConsolidatedDebtView(
            MemberView withMember,
            BigDecimal netAmount,
            String currency,
            String direction) {
    }
}
