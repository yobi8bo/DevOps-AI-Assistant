package com.example.devopsai.prompt.vo;

import java.time.LocalDateTime;

public record PromptTemplateDetail(
        Long id,
        String name,
        String category,
        String content,
        String version,
        Boolean defaultTemplate,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
