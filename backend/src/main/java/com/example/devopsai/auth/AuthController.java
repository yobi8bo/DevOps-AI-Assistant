package com.example.devopsai.auth;

import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserAccountService userAccountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserAccountService userAccountService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var principal = userAccountService.loadByUsername(request.username());
        if (!passwordEncoder.matches(request.password(), principal.getPassword())) {
            log.warn("login_failed username={}", request.username());
            throw new BusinessException(401, "用户名或密码错误");
        }
        userAccountService.recordLogin(principal.getId());

        var userInfo = toUserInfo(principal);
        var response = new LoginResponse(jwtService.createToken(principal), "Bearer", jwtService.expiresSeconds(), userInfo);
        log.info("login_success userId={} username={}", principal.getId(), principal.getUsername());
        return ApiResponse.success("登录成功", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return ApiResponse.success(toUserInfo(principal));
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success("退出成功", true);
    }

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

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserInfo userInfo
    ) {
    }

    public record UserInfo(
            Long id,
            String username,
            String nickname,
            String email,
            List<String> roles,
            List<String> permissions
    ) {
    }
}
