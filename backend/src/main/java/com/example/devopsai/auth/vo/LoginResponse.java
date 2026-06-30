package com.example.devopsai.auth.vo;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo userInfo
) {
}
