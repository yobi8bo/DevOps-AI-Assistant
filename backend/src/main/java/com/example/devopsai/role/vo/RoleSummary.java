package com.example.devopsai.role.vo;

import java.time.LocalDateTime;
import java.util.List;

public record RoleSummary(
        Long id,
        String roleCode,
        String roleName,
        String description,
        Boolean enabled,
        List<String> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
