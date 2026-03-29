package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import java.util.Optional;

public interface UserRepository {

    boolean existsByEmail(Email email);

    void save(User user);

    Optional<User> findByEmail(Email email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByResetToken(String resetToken);

    Optional<User> findById(UserId userId);
}
