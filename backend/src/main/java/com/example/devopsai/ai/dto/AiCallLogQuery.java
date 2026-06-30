package com.example.devopsai.ai.dto;

import java.time.LocalDateTime;

public record AiCallLogQuery(
        String keyword,
        Boolean success,
        Long modelConfigId,
        Long sessionId,
        Long userId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNum,
        long pageSize
) {
}
