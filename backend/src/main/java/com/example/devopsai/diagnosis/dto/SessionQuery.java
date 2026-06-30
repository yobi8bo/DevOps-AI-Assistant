package com.example.devopsai.diagnosis.dto;

public record SessionQuery(
        String keyword,
        String category,
        String status,
        Boolean isProduction,
        long pageNum,
        long pageSize
) {
}
