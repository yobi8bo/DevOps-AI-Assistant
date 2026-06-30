package com.example.devopsai.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        String nickname,
        String email,
        String phone,
        Integer status,
        List<Long> roleIds
) {
}
