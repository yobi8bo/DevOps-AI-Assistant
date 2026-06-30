package com.example.devopsai.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeRequest(
        @NotBlank String title,
        String category,
        String environment,
        String osInfo,
        String middleware,
        String serviceType,
        Boolean isProduction,
        String urgencyLevel,
        String description,
        String logContent,
        String commandOutput,
        Long modelConfigId
) {
}
