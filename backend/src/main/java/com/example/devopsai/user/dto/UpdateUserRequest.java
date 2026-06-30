package com.example.devopsai.user.dto;

public record UpdateUserRequest(
        String username,
        String nickname,
        String email,
        String phone,
        Integer status
) {
}
