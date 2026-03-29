package com.accierta.identity.application.handler;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.ResetToken;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.port.in.RequestPasswordResetCommand;
import com.accierta.identity.domain.port.in.RequestPasswordResetUseCase;
import com.accierta.identity.domain.port.out.PasswordResetEmailSender;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.util.Objects;

public class RequestPasswordResetHandler implements RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetEmailSender passwordResetEmailSender;
    private final Clock clock;

    public RequestPasswordResetHandler(
            UserRepository userRepository,
            PasswordResetEmailSender passwordResetEmailSender,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordResetEmailSender =
                Objects.requireNonNull(passwordResetEmailSender, "passwordResetEmailSender cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void requestPasswordReset(RequestPasswordResetCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        Email email = new Email(command.email());
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        ResetToken resetToken = user.requestPasswordReset(clock);
        userRepository.save(user);
        passwordResetEmailSender.sendPasswordResetEmail(user.email(), resetToken);
    }
}
