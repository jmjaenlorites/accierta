package com.accierta.shared.security;

import com.accierta.identity.domain.model.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserService {

    private final SecretKey secretKey;

    public JwtCurrentUserService(@Value("${accierta.security.jwt-secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public CurrentUser requireUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException("authentication required");
        }
        String token = authorizationHeader.substring("Bearer ".length());
        try {
            String subject = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return new CurrentUser(new UserId(UUID.fromString(subject)));
        } catch (Exception exception) {
            throw new AuthenticationRequiredException("authentication required");
        }
    }
}
