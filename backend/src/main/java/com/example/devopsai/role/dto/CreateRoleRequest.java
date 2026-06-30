package com.example.devopsai.role.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateRoleRequest(
        @NotBlank String roleCode,
        @NotBlank String roleName,
        String description,
        Integer status,
        List<Long> permissionIds
) {
}
