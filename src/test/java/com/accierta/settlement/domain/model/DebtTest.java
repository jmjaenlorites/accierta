package com.accierta.settlement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.settlement.domain.event.DebtSettled;
import com.accierta.settlement.domain.event.PaymentRequested;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DebtTest {

    @Test
    void should_request_payment_when_payer_requests() {
        UserId payer = UserId.newId();
        Debt debt = Debt.generate(
                DebtId.newId(),
                new GroupId(java.util.UUID.randomUUID()),
                new PorraId(java.util.UUID.randomUUID()),
                payer,
                UserId.newId(),
                new BigDecimal("10.00"),
                "€",
                Instant.parse("2026-03-26T10:00:00Z"));

        debt.requestPayment(payer, Instant.parse("2026-03-26T11:00:00Z"));

        assertThat(debt.status()).isEqualTo(DebtStatus.PAYMENT_REQUESTED);
        assertThat(debt.pullUncommittedEvents().getLast()).isInstanceOf(PaymentRequested.class);
    }

    @Test
    void should_reject_request_payment_for_non_payer() {
        Debt debt = Debt.generate(
                DebtId.newId(),
                new GroupId(java.util.UUID.randomUUID()),
                new PorraId(java.util.UUID.randomUUID()),
                UserId.newId(),
                UserId.newId(),
                new BigDecimal("10.00"),
                "€",
                Instant.parse("2026-03-26T10:00:00Z"));

        assertThatThrownBy(() -> debt.requestPayment(UserId.newId(), Instant.parse("2026-03-26T11:00:00Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("only the payer can request payment");
    }

    @Test
    void should_settle_when_receiver_confirms_requested_debt() {
        UserId payer = UserId.newId();
        UserId receiver = UserId.newId();
        Debt debt = Debt.generate(
                DebtId.newId(),
                new GroupId(java.util.UUID.randomUUID()),
                new PorraId(java.util.UUID.randomUUID()),
                payer,
                receiver,
                new BigDecimal("10.00"),
                "€",
                Instant.parse("2026-03-26T10:00:00Z"));
        debt.requestPayment(payer, Instant.parse("2026-03-26T11:00:00Z"));
        debt.pullUncommittedEvents();

        debt.settle(receiver, Instant.parse("2026-03-26T12:00:00Z"));

        assertThat(debt.status()).isEqualTo(DebtStatus.PAID);
        assertThat(debt.pullUncommittedEvents().getLast()).isInstanceOf(DebtSettled.class);
    }

    @Test
    void should_reject_settle_before_payment_requested() {
        UserId receiver = UserId.newId();
        Debt debt = Debt.generate(
                DebtId.newId(),
                new GroupId(java.util.UUID.randomUUID()),
                new PorraId(java.util.UUID.randomUUID()),
                UserId.newId(),
                receiver,
                new BigDecimal("10.00"),
                "€",
                Instant.parse("2026-03-26T10:00:00Z"));

        assertThatThrownBy(() -> debt.settle(receiver, Instant.parse("2026-03-26T11:00:00Z")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("payment not yet requested");
    }
}
