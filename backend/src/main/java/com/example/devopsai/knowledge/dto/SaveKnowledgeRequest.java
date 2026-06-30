package com.example.devopsai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SaveKnowledgeRequest(
        @NotBlank String title,
        String category,
        List<String> tags,
        @NotBlank String content,
        String contentType,
        String sourceType,
        String sourceRef,
        String version,
        Integer status
) {
}
