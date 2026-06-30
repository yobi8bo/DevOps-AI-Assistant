package com.example.devopsai.category.vo;

import java.time.LocalDateTime;

public record CategorySummary(
        Long id,
        String categoryCode,
        String categoryName,
        String description,
        Integer sortOrder,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
