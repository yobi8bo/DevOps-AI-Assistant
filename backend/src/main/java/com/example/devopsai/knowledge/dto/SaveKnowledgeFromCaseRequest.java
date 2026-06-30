package com.example.devopsai.knowledge.dto;

import java.util.List;

public record SaveKnowledgeFromCaseRequest(
        String title,
        String category,
        List<String> tags,
        String content,
        String version,
        Integer status
) {
}
