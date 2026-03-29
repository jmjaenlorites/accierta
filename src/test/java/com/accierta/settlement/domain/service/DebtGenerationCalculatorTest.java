package com.accierta.settlement.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.accierta.betting.domain.model.PorraType;
import com.accierta.settlement.domain.model.SettlementOutcome;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DebtGenerationCalculatorTest {

    private final DebtGenerationCalculator calculator = new DebtGenerationCalculator();

    @Test
    void should_generate_one_debt_per_loser_for_single_winner() {
        UUID winner = UUID.randomUUID();
        UUID loser1 = UUID.randomUUID();
        UUID loser2 = UUID.randomUUID();

        var result = calculator.calculate(
                PorraType.BINARY,
                10,
                null,
                "true",
                List.of(
                        new DebtGenerationCalculator.BetRecord(winner, "true"),
                        new DebtGenerationCalculator.BetRecord(loser1, "false"),
                        new DebtGenerationCalculator.BetRecord(loser2, "false")));

        assertThat(result.debts()).hasSize(2);
        assertThat(result.debts()).allSatisfy(debt -> {
            assertThat(debt.toMemberId()).isEqualTo(winner);
            assertThat(debt.amount()).isEqualByComparingTo("10.00");
        });
        assertThat(result.outcomes()).anySatisfy(outcome -> {
            assertThat(outcome.memberId()).isEqualTo(winner);
            assertThat(outcome.outcome()).isEqualTo(SettlementOutcome.WON);
            assertThat(outcome.moneyChange()).isEqualByComparingTo("20.00");
        });
    }

    @Test
    void should_split_each_loser_stake_across_multiple_winners() {
        UUID winner1 = UUID.randomUUID();
        UUID winner2 = UUID.randomUUID();
        UUID loser1 = UUID.randomUUID();
        UUID loser2 = UUID.randomUUID();

        var result = calculator.calculate(
                PorraType.MULTIPLE_CHOICE,
                10,
                null,
                "Barcelona",
                List.of(
                        new DebtGenerationCalculator.BetRecord(winner1, "Barcelona"),
                        new DebtGenerationCalculator.BetRecord(winner2, "Barcelona"),
                        new DebtGenerationCalculator.BetRecord(loser1, "Madrid"),
                        new DebtGenerationCalculator.BetRecord(loser2, "Madrid")));

        assertThat(result.debts()).hasSize(4);
        assertThat(result.debts()).allSatisfy(debt -> assertThat(debt.amount()).isEqualByComparingTo("5.00"));
        assertThat(result.outcomes()).filteredOn(outcome -> outcome.outcome() == SettlementOutcome.WON)
                .allSatisfy(outcome -> assertThat(outcome.moneyChange()).isEqualByComparingTo("10.00"));
    }

    @Test
    void should_refund_numeric_porra_when_nobody_is_within_margin() {
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();

        var result = calculator.calculate(
                PorraType.NUMERIC,
                10,
                0.15d,
                "100",
                List.of(
                        new DebtGenerationCalculator.BetRecord(member1, "200"),
                        new DebtGenerationCalculator.BetRecord(member2, "300")));

        assertThat(result.debts()).isEmpty();
        assertThat(result.outcomes()).hasSize(2);
        assertThat(result.outcomes()).allSatisfy(outcome -> {
            assertThat(outcome.outcome()).isEqualTo(SettlementOutcome.REFUNDED);
            assertThat(outcome.moneyChange()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }
}
