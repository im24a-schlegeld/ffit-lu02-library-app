package ch.bzz.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import ch.bzz.Config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtHandler {
    private static final String DEFAULT_JWT_SECRET = "library-app-secret-key-1234567890";

    private JwtHandler() {
    }

    private static SecretKey getJwtKey() {
        String secret = Config.getProperties().getOrDefault("jwt.secret", DEFAULT_JWT_SECRET);
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String createJwt(String subject, Integer userId) {
        Date currentTime = new Date();
        Date expirationTime = new Date(currentTime.getTime() + 3_600_000);

        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .issuedAt(currentTime)
                .expiration(expirationTime)
                .signWith(getJwtKey())
                .compact();
    }

    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getJwtKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
