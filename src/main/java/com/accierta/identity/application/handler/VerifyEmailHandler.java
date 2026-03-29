package com.accierta.identity.application.handler;

import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.port.in.VerifyEmailCommand;
import com.accierta.identity.domain.port.in.VerifyEmailUseCase;
import com.accierta.identity.domain.port.out.PendingInvitationAcceptor;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.util.Objects;

public class VerifyEmailHandler implements VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final PendingInvitationAcceptor pendingInvitationAcceptor;
    private final Clock clock;

    public VerifyEmailHandler(
            UserRepository userRepository,
            PendingInvitationAcceptor pendingInvitationAcceptor,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.pendingInvitationAcceptor =
                Objects.requireNonNull(pendingInvitationAcceptor, "pendingInvitationAcceptor cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void verify(VerifyEmailCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        User user = userRepository.findByVerificationToken(command.token())
                .orElseThrow(() -> new VerificationTokenNotFoundException("verification token is invalid"));

        user.verifyEmail(command.token(), clock);
        userRepository.save(user);
        if (user.invitationToken() != null) {
            pendingInvitationAcceptor.acceptPendingInvitation(user.invitationToken(), user.id());
        }
    }
}
