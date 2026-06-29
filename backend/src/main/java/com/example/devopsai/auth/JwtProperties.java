package com.example.devopsai.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * JwtProperties配置属性类，负责承载外部化配置。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expiresSeconds) {
}

