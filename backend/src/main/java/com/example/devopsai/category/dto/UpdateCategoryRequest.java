package com.example.devopsai.category.dto;

public record UpdateCategoryRequest(
        String categoryCode,
        String categoryName,
        String description,
        Integer sortOrder,
        Integer status
) {
}
