package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;

public interface PasswordHasher {

    HashedPassword hash(RawPassword rawPassword);

    boolean matches(RawPassword rawPassword, HashedPassword hashedPassword);
}
