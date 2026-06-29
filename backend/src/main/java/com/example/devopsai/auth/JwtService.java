package com.example.devopsai.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
/**
 * JwtService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class JwtService {

    /**
     * 配置属性。
     */
    private final JwtProperties properties;
    /**
     * JWT签名密钥。
     */
    private final SecretKey key;
    /**
     * 创建JwtService实例。
     * @param properties properties参数。
     */

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 创建JWT令牌。
     * @param principal principal参数。
     * @return 处理结果。
     */

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
    /**
     * 从JWT令牌解析用户名。
     * @param token token参数。
     * @return 处理结果。
     */

    public String parseUsername(String token) {
        return parseClaims(token).getSubject();
    }
    /**
     * 获取JWT有效期。
     * @return 处理结果。
     */

    public long expiresSeconds() {
        return properties.expiresSeconds();
    }
    /**
     * 解析JWT声明。
     * @param token token参数。
     * @return 处理结果。
     */

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

