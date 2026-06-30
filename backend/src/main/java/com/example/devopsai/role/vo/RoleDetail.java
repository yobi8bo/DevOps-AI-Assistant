package com.example.devopsai.role.vo;

import java.time.LocalDateTime;
import java.util.List;

public record RoleDetail(
        Long id,
        String roleCode,
        String roleName,
        String description,
        Boolean enabled,
        List<String> permissions,
        List<Long> permissionIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
