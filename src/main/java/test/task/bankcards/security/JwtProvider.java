package test.task.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import test.task.bankcards.util.enums.RoleType;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final Key key;
    @Getter
    private final long accessTtlMs;
    @Getter
    private final long refreshTtlMs;

    public JwtProvider(
            @Value("${JWT_SECRET}") String secret,
            @Value("${ACCESS_TTL}") long accessTtlMs,
            @Value("${REFRESH_TTL}") long refreshTtlMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public String generateAccessToken(Long userId, RoleType role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setId(UUID.randomUUID().toString())
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessTtlMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setId(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTtlMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
