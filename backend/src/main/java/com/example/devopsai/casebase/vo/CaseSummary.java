package com.example.devopsai.casebase.vo;

import java.time.LocalDateTime;
import java.util.List;

public record CaseSummary(
        Long id,
        Long sourceSessionId,
        String title,
        String category,
        String environment,
        String status,
        List<String> tags,
        Long createdBy,
        Long reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
