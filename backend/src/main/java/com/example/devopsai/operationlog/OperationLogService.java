package com.example.devopsai.operationlog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.operationlog.entity.SysOperationLog;
import com.example.devopsai.operationlog.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogService {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final SysOperationLogMapper operationLogMapper;

    public OperationLogService(SysOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public PageResponse<OperationLogSummary> list(OperationLogQuery query) {
        var wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getCreatedAt);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(SysOperationLog::getUsername, query.keyword())
                    .or()
                    .like(SysOperationLog::getRequestUri, query.keyword())
                    .or()
                    .like(SysOperationLog::getErrorMessage, query.keyword()));
        }
        if (StringUtils.hasText(query.module())) {
            wrapper.eq(SysOperationLog::getModule, query.module());
        }
        if (StringUtils.hasText(query.action())) {
            wrapper.eq(SysOperationLog::getAction, query.action());
        }
        if (query.success() != null) {
            wrapper.eq(SysOperationLog::getSuccess, Boolean.TRUE.equals(query.success()) ? 1 : 0);
        }
        var page = operationLogMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public void record(HttpServletRequest request, int status, Throwable error) {
        var method = request.getMethod();
        var uri = request.getRequestURI();
        if (!shouldRecord(method, uri)) {
            return;
        }
        var log = new SysOperationLog();
        fillPrincipal(log);
        log.setModule(resolveModule(uri));
        log.setAction(resolveAction(method));
        log.setTargetId(resolveTargetId(uri));
        log.setRequestMethod(method);
        log.setRequestUri(limit(uri, 255));
        log.setIpAddress(limit(resolveClientIp(request), 64));
        log.setUserAgent(limit(request.getHeader("User-Agent"), 512));
        log.setSuccess(status < 400 && error == null ? 1 : 0);
        log.setErrorMessage(limit(error == null ? null : error.getMessage(), 2000));
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private boolean shouldRecord(String method, String uri) {
        if (!WRITE_METHODS.contains(method) || !StringUtils.hasText(uri) || !uri.startsWith("/api/")) {
            return false;
        }
        return !uri.startsWith("/api/auth/login");
    }

    private void fillPrincipal(SysOperationLog log) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            return;
        }
        log.setUserId(principal.getId());
        log.setUsername(principal.getUsername());
    }

    private String resolveModule(String uri) {
        var parts = uri.split("/");
        if (parts.length < 3) {
            return "api";
        }
        return switch (parts[2]) {
            case "users" -> "用户管理";
            case "roles", "permissions" -> "角色权限";
            case "fault-categories" -> "故障分类";
            case "cases" -> "案例库";
            case "knowledge" -> "知识库";
            case "reports" -> "复盘报告";
            case "model-configs" -> "模型配置";
            case "prompt-templates" -> "Prompt模板";
            case "diagnosis" -> "智能排障";
            default -> parts[2];
        };
    }

    private String resolveAction(String method) {
        return switch (method) {
            case "POST" -> "新增";
            case "PUT" -> "修改";
            case "PATCH" -> "状态变更";
            case "DELETE" -> "删除";
            default -> method;
        };
    }

    private Long resolveTargetId(String uri) {
        for (var part : uri.split("/")) {
            try {
                return Long.valueOf(part);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        var realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private OperationLogSummary toSummary(SysOperationLog entity) {
        return new OperationLogSummary(
                entity.getId(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getModule(),
                entity.getAction(),
                entity.getTargetId(),
                entity.getRequestMethod(),
                entity.getRequestUri(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                Integer.valueOf(1).equals(entity.getSuccess()),
                entity.getErrorMessage(),
                entity.getCreatedAt()
        );
    }

    public record OperationLogQuery(
            String keyword,
            String module,
            String action,
            Boolean success,
            long pageNum,
            long pageSize
    ) {
    }

    public record OperationLogSummary(
            Long id,
            Long userId,
            String username,
            String module,
            String action,
            Long targetId,
            String requestMethod,
            String requestUri,
            String ipAddress,
            String userAgent,
            Boolean success,
            String errorMessage,
            LocalDateTime createdAt
    ) {
    }
}
