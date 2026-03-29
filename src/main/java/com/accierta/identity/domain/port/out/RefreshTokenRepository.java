package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.RefreshToken;
import com.accierta.identity.domain.model.UserId;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findByValue(String value);

    void invalidate(String value, Instant invalidatedAt);

    void invalidateAllForUser(UserId userId, Instant invalidatedAt);
}
