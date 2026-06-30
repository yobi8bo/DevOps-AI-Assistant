package com.example.devopsai.role.vo;

public record PermissionSummary(
        Long id,
        String permissionCode,
        String permissionName,
        String description
) {
}
