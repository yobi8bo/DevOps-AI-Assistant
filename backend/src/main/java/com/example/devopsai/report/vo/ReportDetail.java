package com.example.devopsai.report.vo;

import java.time.LocalDateTime;

public record ReportDetail(
        Long id,
        Long sessionId,
        String title,
        String format,
        String content,
        Long createdBy,
        LocalDateTime createdAt
) {
}
