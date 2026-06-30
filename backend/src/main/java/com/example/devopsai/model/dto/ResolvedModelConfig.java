package com.example.devopsai.model.dto;

public record ResolvedModelConfig(
        Long id,
        String provider,
        String apiStyle,
        String baseUrl,
        String model,
        String apiKey,
        Integer maxTokens,
        Double temperature,
        Integer timeoutSeconds
) {
}
