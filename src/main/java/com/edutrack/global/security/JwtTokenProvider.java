package com.edutrack.global.security;
import com.edutrack.domain.user.entity.Role;
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
import java.util.Date;
import java.util.Optional;

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

    // Access Token 생성
    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpire);

        return Jwts.builder()
                .setSubject(user.getId().toString())           // 토큰의 subject = userId
                .claim("role", extractRole(user))           // 커스텀 클레임: role
                .setIssuedAt(Date.from(now))                   // 발급 시간
                .setExpiration(Date.from(expiry))              // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)       // 키 + 알고리즘
                .compact();
    }

    // Refresh Token 생성
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
        Optional<Role> anyRole = user.getRoles().stream().findFirst();
        if (anyRole.isEmpty()) {
            return "STUDENT";
        }

        return anyRole.get().getName();
    }

    // 토큰에서 Claims 파싱
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)               // 서명 검증에 사용할 키
                .build()
                .parseClaimsJws(token)            // 서명 검증 + 파싱
                .getBody();                       // Claims 반환
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        String subject = claims.getSubject();
        return Long.parseLong(subject);
    }

    // 토큰에서 role 클레임 추출
    public String getRole(String token) {
        Claims claims = parseClaims(token);
        Object role = claims.get("role");
        if (role == null) {
            return null;
        }
        return role.toString();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }
}
