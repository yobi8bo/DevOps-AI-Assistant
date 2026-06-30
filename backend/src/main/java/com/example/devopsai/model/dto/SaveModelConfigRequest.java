package com.example.devopsai.model.dto;

public record SaveModelConfigRequest(
        String provider,
        String apiStyle,
        String modelName,
        String apiBaseUrl,
        String apiKey,
        Integer maxTokens,
        Double temperature,
        Integer timeoutSeconds,
        Boolean defaultModel,
        Integer status
) {
}
