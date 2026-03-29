package com.accierta.identity.application.handler;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.VerificationToken;
import com.accierta.identity.domain.port.in.ResendVerificationCommand;
import com.accierta.identity.domain.port.in.ResendVerificationUseCase;
import com.accierta.identity.domain.port.out.UserRepository;
import com.accierta.identity.domain.port.out.VerificationEmailSender;
import java.time.Clock;
import java.util.Objects;

public class ResendVerificationHandler implements ResendVerificationUseCase {

    private final UserRepository userRepository;
    private final VerificationEmailSender verificationEmailSender;
    private final Clock clock;

    public ResendVerificationHandler(
            UserRepository userRepository,
            VerificationEmailSender verificationEmailSender,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.verificationEmailSender =
                Objects.requireNonNull(verificationEmailSender, "verificationEmailSender cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void resend(ResendVerificationCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        Email email = new Email(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (user.isVerified()) {
            throw new AlreadyVerifiedException("email is already verified");
        }

        VerificationToken verificationToken = user.resendVerification(clock);
        userRepository.save(user);
        verificationEmailSender.sendVerificationEmail(user.email(), verificationToken);
    }
}
