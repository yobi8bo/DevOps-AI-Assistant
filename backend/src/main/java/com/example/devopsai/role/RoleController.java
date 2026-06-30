package com.example.devopsai.role;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.role.dto.CreateRoleRequest;
import com.example.devopsai.role.dto.RoleQuery;
import com.example.devopsai.role.dto.UpdateRolePermissionsRequest;
import com.example.devopsai.role.dto.UpdateRoleRequest;
import com.example.devopsai.role.dto.UpdateRoleStatusRequest;
import com.example.devopsai.role.vo.RoleDetail;
import com.example.devopsai.role.vo.RoleSummary;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<PageResponse<RoleSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(roleService.list(new RoleQuery(keyword, status, pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleDetail> get(@PathVariable Long id) {
        return ApiResponse.success(roleService.get(id));
    }

    @PostMapping
    public ApiResponse<RoleDetail> create(
            @Valid @RequestBody CreateRoleRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", roleService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", roleService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateRoleStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        roleService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }

    @PutMapping("/{id}/permissions")
    public ApiResponse<Boolean> updatePermissions(
            @PathVariable Long id,
            @RequestBody UpdateRolePermissionsRequest request
    ) {
        roleService.updatePermissions(id, request);
        return ApiResponse.success("权限已更新", true);
    }
}
