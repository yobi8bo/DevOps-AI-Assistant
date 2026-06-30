package com.example.devopsai.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String categoryCode,
        @NotBlank String categoryName,
        String description,
        Integer sortOrder,
        Integer status
) {
}
