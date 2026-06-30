package com.example.devopsai.role;

import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.role.RoleService.PermissionSummary;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final RoleService roleService;

    public PermissionController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<PermissionSummary>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(roleService.listPermissions(keyword));
    }
}
