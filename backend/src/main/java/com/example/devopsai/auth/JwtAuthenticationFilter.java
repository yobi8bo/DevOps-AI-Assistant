package com.example.devopsai.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * JwtAuthenticationFilter过滤器，负责处理请求过滤逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Bearer Token请求头前缀。
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * JWT令牌服务。
     */
    private final JwtService jwtService;
    /**
     * 用户账号服务。
     */
    private final UserAccountService userAccountService;
    /**
     * 创建JwtAuthenticationFilter实例。
     * @param jwtService jwtService参数。
     * @param userAccountService userAccountService参数。
     */

    public JwtAuthenticationFilter(JwtService jwtService, UserAccountService userAccountService) {
        this.jwtService = jwtService;
        this.userAccountService = userAccountService;
    }
    /**
     * 执行JWT认证过滤。
     * @param request request参数。
     * @param response response参数。
     * @param filterChain filterChain参数。
     * @throws ServletException ServletException异常。
     * @throws IOException IOException异常。
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var username = jwtService.parseUsername(token);
                var principal = userAccountService.loadByUsername(username);
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
    /**
     * 从请求头解析JWT令牌。
     * @param request request参数。
     * @return 处理结果。
     */

    private String resolveToken(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}

