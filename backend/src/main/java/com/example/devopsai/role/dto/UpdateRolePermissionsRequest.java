package com.example.devopsai.role.dto;

import java.util.List;

public record UpdateRolePermissionsRequest(List<Long> permissionIds) {
}
