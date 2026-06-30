package com.example.devopsai.operationlog.vo;

import java.time.LocalDateTime;

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
