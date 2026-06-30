package com.example.devopsai.config;

import com.example.devopsai.common.security.SensitiveDataMasker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * RequestLoggingFilter过滤器，负责处理请求过滤逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    /**
     * 请求ID。
     */
    private static final String REQUEST_ID = "requestId";
    /**
     * 执行JWT认证过滤。
     * @param request request参数。
     * @param response response参数。
     * @param filterChain filterChain参数。
     * @throws ServletException ServletException异常。
     * @throws IOException IOException异常。
     */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        var startedAt = System.currentTimeMillis();
        MDC.put(REQUEST_ID, requestId);
        response.setHeader("X-Request-Id", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            var durationMs = System.currentTimeMillis() - startedAt;
            log.info(
                    "http_request method={} uri={} query={} status={} durationMs={} remoteAddr={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    SensitiveDataMasker.maskInline(request.getQueryString()),
                    response.getStatus(),
                    durationMs,
                    request.getRemoteAddr()
            );
            MDC.remove(REQUEST_ID);
        }
    }
}
