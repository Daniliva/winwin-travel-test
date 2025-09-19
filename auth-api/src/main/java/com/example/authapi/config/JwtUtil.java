package com.example.authapi.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour in milliseconds

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId) {
        if (userId == null) {
            log.warn("User ID is null when generating token");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        log.debug("Generating token for user ID: {}", userId);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.debug("Extracted user ID: {} from token", claims.getSubject());
            return UUID.fromString(claims.getSubject());
        } catch (JwtException e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            log.debug("Token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage(), e);
            return false;
        }
    }
}