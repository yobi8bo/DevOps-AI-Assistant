package com.example.devopsai.user.vo;

import java.time.LocalDateTime;
import java.util.List;

public record UserDetail(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        Boolean enabled,
        List<String> roles,
        List<Long> roleIds,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
