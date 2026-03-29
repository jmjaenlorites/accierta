package com.accierta.identity.application.handler;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.RegisterUserCommand;
import com.accierta.identity.domain.port.in.RegisterUserUseCase;
import com.accierta.identity.domain.port.out.PasswordHasher;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.util.Objects;

public class RegisterUserHandler implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public RegisterUserHandler(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public UserId register(RegisterUserCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("email already in use");
        }

        RawPassword rawPassword = new RawPassword(command.password());
        HashedPassword hashedPassword = passwordHasher.hash(rawPassword);
        User user = User.register(
                UserId.newId(),
                email,
                rawPassword,
                hashedPassword,
                command.invitationToken(),
                clock);

        userRepository.save(user);
        return user.id();
    }
}
