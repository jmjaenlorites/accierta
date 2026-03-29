package com.accierta.identity.infrastructure.security;

import com.accierta.identity.domain.model.AccessToken;
import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.out.TokenIssuer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JjwtTokenIssuer implements TokenIssuer {

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    private final SecretKey secretKey;

    public JjwtTokenIssuer(@Value("${accierta.security.jwt-secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public AccessToken issueAccessToken(UserId userId, Email email, Instant issuedAt) {
        Instant expiresAt = issuedAt.plus(ACCESS_TOKEN_TTL);
        String token = Jwts.builder()
                .subject(userId.value().toString())
                .claim("email", email.value())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new AccessToken(token, expiresAt);
    }
}
