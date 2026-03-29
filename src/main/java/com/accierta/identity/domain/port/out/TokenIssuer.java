package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.AccessToken;
import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.UserId;
import java.time.Instant;

public interface TokenIssuer {

    AccessToken issueAccessToken(UserId userId, Email email, Instant issuedAt);
}
