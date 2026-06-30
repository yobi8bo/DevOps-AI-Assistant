package com.example.devopsai.knowledge.dto;

public record KnowledgeQuery(
        String keyword,
        String category,
        String tag,
        Integer status,
        long pageNum,
        long pageSize
) {
}
