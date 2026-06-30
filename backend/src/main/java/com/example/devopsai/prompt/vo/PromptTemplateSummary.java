package com.example.devopsai.prompt.vo;

import java.time.LocalDateTime;

public record PromptTemplateSummary(
        Long id,
        String name,
        String category,
        String version,
        Boolean defaultTemplate,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
