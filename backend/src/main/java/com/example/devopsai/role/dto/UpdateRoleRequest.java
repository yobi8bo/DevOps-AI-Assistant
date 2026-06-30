package com.example.devopsai.role.dto;

public record UpdateRoleRequest(
        String roleCode,
        String roleName,
        String description,
        Integer status
) {
}
