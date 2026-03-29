package com.accierta.settlement.application.query;

import java.math.BigDecimal;

public record MemberStatsView(
        int totalPorras,
        int won,
        int lost,
        int cancelled,
        BigDecimal winRatio,
        int currentStreak,
        BigDecimal totalMoneyWon,
        BigDecimal totalMoneyLost,
        BigDecimal netMoney,
        BigDecimal moneyRatio,
        BigDecimal pendingDebtsOwed,
        BigDecimal pendingDebtsOwing,
        String currency) {
}
