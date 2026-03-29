package com.accierta.settlement.domain.service;

import com.accierta.betting.domain.model.PorraType;
import com.accierta.settlement.domain.model.SettlementOutcome;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DebtGenerationCalculator {

    public SettlementCalculationResult calculate(
            PorraType type,
            Integer stake,
            Double tolerance,
            String acceptedResult,
            List<BetRecord> bets) {
        if (bets == null || bets.isEmpty()) {
            return SettlementCalculationResult.refunded(List.of());
        }
        List<UUID> winners = determineWinners(type, tolerance, acceptedResult, bets);
        if (winners.isEmpty()) {
            return SettlementCalculationResult.refunded(bets.stream()
                    .map(bet -> new MemberOutcomeRecord(bet.memberId(), SettlementOutcome.REFUNDED, BigDecimal.ZERO))
                    .toList());
        }

        List<UUID> losers = bets.stream()
                .map(BetRecord::memberId)
                .filter(memberId -> !winners.contains(memberId))
                .toList();
        if (losers.isEmpty()) {
            return SettlementCalculationResult.refunded(bets.stream()
                    .map(bet -> new MemberOutcomeRecord(bet.memberId(), SettlementOutcome.REFUNDED, BigDecimal.ZERO))
                    .toList());
        }

        long stakeCents = BigDecimal.valueOf(stake.longValue())
                .movePointRight(2)
                .longValueExact();
        List<DebtInstruction> debts = new ArrayList<>();
        Map<UUID, BigDecimal> moneyChange = new HashMap<>();
        for (BetRecord bet : bets) {
            moneyChange.put(bet.memberId(), BigDecimal.ZERO);
        }

        for (UUID loser : losers) {
            List<UUID> shuffledWinners = new ArrayList<>(winners);
            for (int index = shuffledWinners.size() - 1; index > 0; index--) {
                int swapIndex = ThreadLocalRandom.current().nextInt(index + 1);
                UUID current = shuffledWinners.get(index);
                shuffledWinners.set(index, shuffledWinners.get(swapIndex));
                shuffledWinners.set(swapIndex, current);
            }
            long baseShare = stakeCents / winners.size();
            long remainder = stakeCents % winners.size();
            for (int index = 0; index < shuffledWinners.size(); index++) {
                long share = baseShare + (index < remainder ? 1 : 0);
                if (share == 0) {
                    continue;
                }
                BigDecimal amount = BigDecimal.valueOf(share, 2);
                UUID winner = shuffledWinners.get(index);
                debts.add(new DebtInstruction(loser, winner, amount));
                moneyChange.put(loser, moneyChange.get(loser).subtract(amount));
                moneyChange.put(winner, moneyChange.get(winner).add(amount));
            }
        }

        List<MemberOutcomeRecord> outcomes = bets.stream()
                .map(bet -> new MemberOutcomeRecord(
                        bet.memberId(),
                        winners.contains(bet.memberId()) ? SettlementOutcome.WON : SettlementOutcome.LOST,
                        moneyChange.getOrDefault(bet.memberId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)))
                .toList();
        return new SettlementCalculationResult(debts, outcomes);
    }

    private List<UUID> determineWinners(
            PorraType type,
            Double tolerance,
            String acceptedResult,
            List<BetRecord> bets) {
        return switch (type) {
            case BINARY, MULTIPLE_CHOICE -> bets.stream()
                    .filter(bet -> acceptedResult.equals(bet.prediction()))
                    .map(BetRecord::memberId)
                    .toList();
            case NUMERIC -> determineNumericWinners(tolerance, acceptedResult, bets);
        };
    }

    private List<UUID> determineNumericWinners(
            Double tolerance,
            String acceptedResult,
            List<BetRecord> bets) {
        BigDecimal actual = new BigDecimal(acceptedResult);
        List<BigDecimal> distances = bets.stream()
                .map(bet -> new BigDecimal(bet.prediction()).subtract(actual).abs())
                .sorted()
                .toList();
        if (distances.isEmpty()) {
            return List.of();
        }
        BigDecimal median;
        int size = distances.size();
        if (size % 2 == 1) {
            median = distances.get(size / 2);
        } else {
            median = distances.get(size / 2 - 1)
                    .add(distances.get(size / 2))
                    .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
        }
        BigDecimal margin = median.multiply(BigDecimal.valueOf(tolerance == null ? 0d : tolerance))
                .setScale(6, RoundingMode.HALF_UP);
        List<BetDistance> eligible = bets.stream()
                .map(bet -> new BetDistance(
                        bet.memberId(),
                        new BigDecimal(bet.prediction()).subtract(actual).abs()))
                .filter(distance -> distance.distance().compareTo(margin) <= 0)
                .toList();
        if (eligible.isEmpty()) {
            return List.of();
        }
        BigDecimal bestDistance = eligible.stream()
                .map(BetDistance::distance)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        return eligible.stream()
                .filter(distance -> distance.distance().compareTo(bestDistance) == 0)
                .map(BetDistance::memberId)
                .toList();
    }

    private record BetDistance(
            UUID memberId,
            BigDecimal distance) {
    }

    public record BetRecord(
            UUID memberId,
            String prediction) {
    }

    public record DebtInstruction(
            UUID fromMemberId,
            UUID toMemberId,
            BigDecimal amount) {
    }

    public record MemberOutcomeRecord(
            UUID memberId,
            SettlementOutcome outcome,
            BigDecimal moneyChange) {
    }

    public record SettlementCalculationResult(
            List<DebtInstruction> debts,
            List<MemberOutcomeRecord> outcomes) {

        public static SettlementCalculationResult refunded(List<MemberOutcomeRecord> outcomes) {
            return new SettlementCalculationResult(List.of(), outcomes);
        }
    }
}
