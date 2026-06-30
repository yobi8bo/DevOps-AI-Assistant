package com.example.devopsai.knowledge.vo;

import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeSummary(
        Long id,
        String title,
        String category,
        List<String> tags,
        String contentType,
        String sourceType,
        String sourceRef,
        String version,
        Boolean enabled,
        Long createdBy,
        Long updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
