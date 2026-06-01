package com.pairflow.config;

import com.pairflow.common.error.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/** Issues and verifies stateless HS256 JWTs. Subject = userId, with an email claim. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationHours;

    public JwtService(@Value("${pairflow.jwt.secret}") String secret,
                      @Value("${pairflow.jwt.expiration-hours}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    public String issue(String userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    public AuthUser parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return new AuthUser(claims.getSubject(), claims.get("email", String.class));
        } catch (Exception e) {
            throw ApiException.unauthorized("Invalid or expired token");
        }
    }
}
