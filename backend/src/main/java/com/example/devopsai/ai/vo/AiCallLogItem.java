package com.example.devopsai.ai.vo;

import java.time.LocalDateTime;

public record AiCallLogItem(
        Long id,
        String requestId,
        Long userId,
        Long sessionId,
        Long modelConfigId,
        String provider,
        String modelName,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        Integer latencyMs,
        Boolean success,
        String errorCode,
        String errorMessage,
        LocalDateTime createdAt
) {
}
