package com.example.devopsai.casebase.dto;

public record CaseQuery(
        String keyword,
        String category,
        String status,
        String tag,
        long pageNum,
        long pageSize
) {
}
