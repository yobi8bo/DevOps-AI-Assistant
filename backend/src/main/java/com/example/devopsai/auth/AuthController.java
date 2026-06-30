package com.example.devopsai.auth;

import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.ErrorCode;
import com.example.devopsai.auth.dto.ChangePasswordRequest;
import com.example.devopsai.auth.dto.LoginRequest;
import com.example.devopsai.auth.vo.LoginResponse;
import com.example.devopsai.auth.vo.UserInfo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * AuthController控制器，负责处理对应模块的HTTP请求。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * 用户账号服务。
     */
    private final UserAccountService userAccountService;
    /**
     * 密码编码器。
     */
    private final PasswordEncoder passwordEncoder;
    /**
     * JWT令牌服务。
     */
    private final JwtService jwtService;
    /**
     * 创建AuthController实例。
     * @param userAccountService userAccountService参数。
     * @param passwordEncoder passwordEncoder参数。
     * @param jwtService jwtService参数。
     */

    public AuthController(
            UserAccountService userAccountService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    /**
     * 执行login处理逻辑。
     * @param request request参数。
     * @return 处理结果。
     */

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var principal = userAccountService.loadByUsername(request.username());
        if (!passwordEncoder.matches(request.password(), principal.getPassword())) {
            log.warn("login_failed userId={}", principal.getId());
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED, "用户名或密码错误");
        }
        userAccountService.recordLogin(principal.getId());

        var userInfo = toUserInfo(principal);
        var response = new LoginResponse(
                jwtService.createToken(principal),
                "Bearer",
                jwtService.expiresSeconds(),
                userInfo
        );
        log.info("login_success userId={} username={}", principal.getId(), principal.getUsername());
        return ApiResponse.success("登录成功", response);
    }
    /**
     * 执行me处理逻辑。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @GetMapping("/me")
    public ApiResponse<UserInfo> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return ApiResponse.success(toUserInfo(principal));
    }
    /**
     * 执行logout处理逻辑。
     * @return 处理结果。
     */

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success("退出成功", true);
    }

    @PostMapping("/change-password")
    public ApiResponse<Boolean> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        userAccountService.changePassword(
                principal.getId(),
                request.oldPassword(),
                request.newPassword(),
                passwordEncoder
        );
        return ApiResponse.success("密码已修改，请重新登录", true);
    }
    /**
     * 转换为前端用户信息。
     * @param principal principal参数。
     * @return 处理结果。
     */

    private UserInfo toUserInfo(AppUserPrincipal principal) {
        return new UserInfo(
                principal.getId(),
                principal.getUsername(),
                principal.getNickname(),
                principal.getEmail(),
                principal.getRoles(),
                principal.getPermissions()
        );
    }
}
