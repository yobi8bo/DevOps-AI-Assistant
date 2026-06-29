package com.example.devopsai.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(AppUserPrincipal principal) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("roles", principal.getRoles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.expiresSeconds())))
                .signWith(key)
                .compact();
    }

    public String parseUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public long expiresSeconds() {
        return properties.expiresSeconds();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

