package com.edutrack.global.security;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import static com.edutrack.domain.user.util.RoleUtils.extractPrimaryRoleName;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpire;
    private final long refreshTokenExpire;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expire-ms}") long accessTokenExpire,
            @Value("${jwt.refresh-expire-ms}") long refreshTokenExpire
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpire = accessTokenExpire;
        this.refreshTokenExpire = refreshTokenExpire;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpire);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("role", extractPrimaryRoleName(user))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpire);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String extractRole(User user) {
        return user.getRoles().stream()
                .map(Role::getName)                               // RoleType
                .min(Comparator.comparingInt(this::priority))     // 최상위 Role 1개
                .map(Enum::name)                                  // RoleType -> String
                .orElse("STUDENT");
    }

    private int priority(RoleType roleType) {
        return switch (roleType) {
            case ADMIN -> 1;
            case PRINCIPAL -> 2;
            case TEACHER -> 3;
            case STUDENT -> 4;
        };
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);  // "ADMIN"/"STUDENT" 등
    }
}