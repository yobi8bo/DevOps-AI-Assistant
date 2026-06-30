package com.example.devopsai.prompt.dto;

public record SavePromptTemplateRequest(
        String name,
        String category,
        String content,
        String version,
        Boolean defaultTemplate,
        Integer status
) {
}
