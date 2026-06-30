package com.example.devopsai.diagnosis.vo;

import java.time.LocalDateTime;

public record SessionSummary(
        Long id,
        String title,
        String category,
        String environment,
        Boolean isProduction,
        String urgencyLevel,
        String status,
        String riskLevel,
        String summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
