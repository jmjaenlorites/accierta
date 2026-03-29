package com.accierta.identity.infrastructure.security;

import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public HashedPassword hash(RawPassword rawPassword) {
        return new HashedPassword(encoder.encode(rawPassword.value()));
    }

    @Override
    public boolean matches(RawPassword rawPassword, HashedPassword hashedPassword) {
        return encoder.matches(rawPassword.value(), hashedPassword.value());
    }
}
