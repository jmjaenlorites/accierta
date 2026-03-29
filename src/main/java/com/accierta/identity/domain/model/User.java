package com.accierta.identity.domain.model;

import com.accierta.identity.domain.event.EmailVerified;
import com.accierta.identity.domain.event.PasswordReset;
import com.accierta.identity.domain.event.PasswordResetRequested;
import com.accierta.identity.domain.event.UserRegistered;
import com.accierta.identity.domain.event.VerificationEmailResent;
import com.accierta.shared.domain.AggregateRoot;
import com.accierta.shared.domain.DomainEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class User extends AggregateRoot {

    private UserId id;
    private Email email;
    private HashedPassword hashedPassword;
    private VerificationToken verificationToken;
    private ResetToken resetToken;
    private boolean verified;
    private String invitationToken;

    private User() {
    }

    public static User register(
            UserId userId,
            Email email,
            RawPassword rawPassword,
            HashedPassword hashedPassword,
            String invitationToken,
            Clock clock) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(rawPassword, "rawPassword cannot be null");
        Objects.requireNonNull(hashedPassword, "hashedPassword cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        User user = new User();
        user.verificationToken = VerificationToken.create(clock);
        user.emit(new UserRegistered(userId, email, invitationToken, Instant.now(clock)));
        user.hashedPassword = hashedPassword;
        return user;
    }

    public static User reconstitute(List<DomainEvent> history, HashedPassword hashedPassword, VerificationToken verificationToken) {
        return reconstitute(history, hashedPassword, verificationToken, null);
    }

    public static User reconstitute(
            List<DomainEvent> history,
            HashedPassword hashedPassword,
            VerificationToken verificationToken,
            ResetToken resetToken) {
        Objects.requireNonNull(history, "history cannot be null");
        User user = new User();
        history.forEach(user::replay);
        user.hashedPassword = hashedPassword;
        user.verificationToken = verificationToken;
        user.resetToken = resetToken;
        return user;
    }

    public boolean verifyEmail(String tokenValue, Clock clock) {
        Objects.requireNonNull(tokenValue, "tokenValue cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        if (verified) {
            return false;
        }
        if (verificationToken == null || !verificationToken.value().equals(tokenValue)) {
            throw new IllegalArgumentException("verification token is invalid");
        }
        if (verificationToken.isExpiredAt(Instant.now(clock))) {
            throw new IllegalStateException("verification token has expired");
        }

        emit(new EmailVerified(id, Instant.now(clock)));
        return true;
    }

    public VerificationToken resendVerification(Clock clock) {
        Objects.requireNonNull(clock, "clock cannot be null");

        if (verified) {
            throw new IllegalStateException("user is already verified");
        }

        verificationToken = VerificationToken.create(clock);
        emit(new VerificationEmailResent(id, email, Instant.now(clock)));
        return verificationToken;
    }

    public ResetToken requestPasswordReset(Clock clock) {
        Objects.requireNonNull(clock, "clock cannot be null");

        resetToken = ResetToken.create(clock);
        emit(new PasswordResetRequested(id, email, Instant.now(clock)));
        return resetToken;
    }

    public void resetPassword(String tokenValue, HashedPassword hashedPassword, Clock clock) {
        Objects.requireNonNull(tokenValue, "tokenValue cannot be null");
        Objects.requireNonNull(hashedPassword, "hashedPassword cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        validateResetToken(tokenValue, clock);

        this.hashedPassword = hashedPassword;
        this.resetToken = null;
        emit(new PasswordReset(id, Instant.now(clock)));
    }

    public void validateResetToken(String tokenValue, Clock clock) {
        Objects.requireNonNull(tokenValue, "tokenValue cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        if (resetToken == null || !resetToken.value().equals(tokenValue)) {
            throw new IllegalArgumentException("reset token is invalid");
        }
        if (resetToken.isExpiredAt(Instant.now(clock))) {
            throw new IllegalStateException("reset token has expired");
        }
    }

    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof UserRegistered userRegistered) {
            this.id = userRegistered.userId();
            this.email = userRegistered.email();
            this.invitationToken = userRegistered.invitationToken();
        } else if (event instanceof EmailVerified) {
            this.verified = true;
        } else if (event instanceof VerificationEmailResent) {
            // Event confirms the action; token state is held in the aggregate and will later
            // be persisted through the event store/outbox adapters we add in infrastructure.
        } else if (event instanceof PasswordResetRequested) {
            // Reset token is tracked directly on the aggregate and persisted through the projection.
        } else if (event instanceof PasswordReset) {
            this.resetToken = null;
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public boolean isVerified() {
        return verified;
    }

    public VerificationToken verificationToken() {
        return verificationToken;
    }

    public ResetToken resetToken() {
        return resetToken;
    }

    public HashedPassword hashedPassword() {
        return hashedPassword;
    }

    public String invitationToken() {
        return invitationToken;
    }
}
