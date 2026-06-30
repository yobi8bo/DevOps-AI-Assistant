package com.example.devopsai.auth.vo;

import java.util.List;

public record UserInfo(
        Long id,
        String username,
        String nickname,
        String email,
        List<String> roles,
        List<String> permissions
) {
}
