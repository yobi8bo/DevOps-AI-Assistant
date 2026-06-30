package com.example.devopsai.model.vo;

import java.time.LocalDateTime;

public record ModelConfigDetail(
        Long id,
        String provider,
        String apiStyle,
        String modelName,
        String apiBaseUrl,
        Integer maxTokens,
        Double temperature,
        Integer timeoutSeconds,
        Boolean defaultModel,
        Boolean enabled,
        Boolean hasApiKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
