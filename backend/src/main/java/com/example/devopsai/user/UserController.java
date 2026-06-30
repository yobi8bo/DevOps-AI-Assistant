package com.example.devopsai.user;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.user.dto.CreateUserRequest;
import com.example.devopsai.user.dto.ResetPasswordRequest;
import com.example.devopsai.user.dto.UpdateUserRequest;
import com.example.devopsai.user.dto.UpdateUserRolesRequest;
import com.example.devopsai.user.dto.UpdateUserStatusRequest;
import com.example.devopsai.user.dto.UserQuery;
import com.example.devopsai.user.vo.UserDetail;
import com.example.devopsai.user.vo.UserSummary;
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
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(userService.list(new UserQuery(keyword, status, pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetail> get(@PathVariable Long id) {
        return ApiResponse.success(userService.get(id));
    }

    @PostMapping
    public ApiResponse<UserDetail> create(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", userService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", userService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Boolean> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        userService.resetPassword(id, request, principal.getId());
        return ApiResponse.success("密码已重置", true);
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Boolean> updateRoles(
            @PathVariable Long id,
            @RequestBody UpdateUserRolesRequest request
    ) {
        userService.updateRoles(id, request);
        return ApiResponse.success("角色已更新", true);
    }
}
