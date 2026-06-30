package com.example.devopsai.report.vo;

import java.time.LocalDateTime;

public record ReportSummary(
        Long id,
        Long sessionId,
        String title,
        String format,
        Long createdBy,
        LocalDateTime createdAt
) {
}
