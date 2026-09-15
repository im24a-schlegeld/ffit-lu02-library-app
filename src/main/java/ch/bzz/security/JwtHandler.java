package ch.bzz.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import ch.bzz.Config;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtHandler {
    private static final String JWT_SECRET = Config.getProperties().getOrDefault("jwt.secret", "library-app-secret-key-1234567890");
    private static final SecretKey JWT_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtHandler() {
    }

    public static String createJwt(String subject, Integer userId) {
        Date currentTime = new Date();
        Date expirationTime = new Date(currentTime.getTime() + 3_600_000);

        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .issuedAt(currentTime)
                .expiration(expirationTime)
                .signWith(JWT_KEY)
                .compact();
    }
}
