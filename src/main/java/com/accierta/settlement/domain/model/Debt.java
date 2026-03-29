package com.accierta.settlement.domain.model;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.groups.domain.model.GroupId;
import com.accierta.identity.domain.model.UserId;
import com.accierta.settlement.domain.event.DebtGenerated;
import com.accierta.settlement.domain.event.DebtSettled;
import com.accierta.settlement.domain.event.PaymentRequested;
import com.accierta.shared.domain.AggregateRoot;
import com.accierta.shared.domain.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Debt extends AggregateRoot {

    private DebtId id;
    private GroupId groupId;
    private PorraId porraId;
    private UserId fromMemberId;
    private UserId toMemberId;
    private BigDecimal amount;
    private String currency;
    private DebtStatus status;
    private Instant createdAt;
    private Instant requestedAt;
    private Instant settledAt;

    private Debt() {
    }

    public static Debt generate(
            DebtId debtId,
            GroupId groupId,
            PorraId porraId,
            UserId fromMemberId,
            UserId toMemberId,
            BigDecimal amount,
            String currency,
            Instant now) {
        Objects.requireNonNull(debtId, "debtId cannot be null");
        Objects.requireNonNull(groupId, "groupId cannot be null");
        Objects.requireNonNull(porraId, "porraId cannot be null");
        Objects.requireNonNull(fromMemberId, "fromMemberId cannot be null");
        Objects.requireNonNull(toMemberId, "toMemberId cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("payer and receiver must be different");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        Debt debt = new Debt();
        debt.emit(new DebtGenerated(
                debtId,
                groupId,
                porraId,
                fromMemberId,
                toMemberId,
                amount,
                currency.trim(),
                now));
        return debt;
    }

    public void requestPayment(UserId requesterId, Instant now) {
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (status == DebtStatus.PAID) {
            throw new IllegalStateException("already settled");
        }
        if (status == DebtStatus.PAYMENT_REQUESTED) {
            throw new IllegalStateException("payment already requested");
        }
        if (!requesterId.equals(fromMemberId)) {
            throw new IllegalArgumentException("only the payer can request payment");
        }
        emit(new PaymentRequested(id, requesterId, now));
    }

    public void settle(UserId confirmerId, Instant now) {
        Objects.requireNonNull(confirmerId, "confirmerId cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        if (status == DebtStatus.PAID) {
            throw new IllegalStateException("already settled");
        }
        if (status != DebtStatus.PAYMENT_REQUESTED) {
            throw new IllegalStateException("payment not yet requested");
        }
        if (!confirmerId.equals(toMemberId)) {
            throw new IllegalArgumentException("only the receiver can settle");
        }
        emit(new DebtSettled(id, confirmerId, now));
    }

    public static Debt reconstitute(
            DebtId debtId,
            GroupId groupId,
            PorraId porraId,
            UserId fromMemberId,
            UserId toMemberId,
            BigDecimal amount,
            String currency,
            DebtStatus status,
            Instant createdAt,
            Instant requestedAt,
            Instant settledAt,
            long version) {
        Debt debt = new Debt();
        debt.id = debtId;
        debt.groupId = groupId;
        debt.porraId = porraId;
        debt.fromMemberId = fromMemberId;
        debt.toMemberId = toMemberId;
        debt.amount = amount;
        debt.currency = currency;
        debt.status = status;
        debt.createdAt = createdAt;
        debt.requestedAt = requestedAt;
        debt.settledAt = settledAt;
        debt.rehydrateToVersion(version);
        return debt;
    }

    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof DebtGenerated generated) {
            id = generated.debtId();
            groupId = generated.groupId();
            porraId = generated.porraId();
            fromMemberId = generated.fromMemberId();
            toMemberId = generated.toMemberId();
            amount = generated.amount();
            currency = generated.currency();
            status = DebtStatus.PENDING;
            createdAt = generated.occurredAt();
        } else if (event instanceof PaymentRequested requested) {
            status = DebtStatus.PAYMENT_REQUESTED;
            requestedAt = requested.occurredAt();
        } else if (event instanceof DebtSettled settled) {
            status = DebtStatus.PAID;
            settledAt = settled.occurredAt();
        }
    }

    public DebtId id() {
        return id;
    }

    public GroupId groupId() {
        return groupId;
    }

    public PorraId porraId() {
        return porraId;
    }

    public UserId fromMemberId() {
        return fromMemberId;
    }

    public UserId toMemberId() {
        return toMemberId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public DebtStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant requestedAt() {
        return requestedAt;
    }

    public Instant settledAt() {
        return settledAt;
    }
}
