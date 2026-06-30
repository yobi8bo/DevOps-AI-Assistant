package com.example.devopsai.diagnosis.vo;

import java.time.LocalDateTime;
import java.util.List;

public record SessionDetail(
        Long id,
        String title,
        String category,
        String environment,
        Boolean isProduction,
        String urgencyLevel,
        String status,
        List<MessageItem> messages,
        ResultItem latestResult,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
