package com.accierta.identity.application.handler;

import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.port.in.ResetPasswordCommand;
import com.accierta.identity.domain.port.in.ResetPasswordUseCase;
import com.accierta.identity.domain.port.out.PasswordHasher;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ResetPasswordHandler implements ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public ResetPasswordHandler(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            RefreshTokenRepository refreshTokenRepository,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
        this.refreshTokenRepository =
                Objects.requireNonNull(refreshTokenRepository, "refreshTokenRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        User user = userRepository.findByResetToken(command.token())
                .orElseThrow(() -> new ResetTokenNotFoundException("reset token is invalid"));

        user.validateResetToken(command.token(), clock);
        RawPassword rawPassword = new RawPassword(command.newPassword());
        HashedPassword hashedPassword = passwordHasher.hash(rawPassword);

        user.resetPassword(command.token(), hashedPassword, clock);
        userRepository.save(user);
        refreshTokenRepository.invalidateAllForUser(user.id(), Instant.now(clock));
    }
}
