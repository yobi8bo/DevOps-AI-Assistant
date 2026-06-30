package com.example.devopsai.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;

public record FollowUpRequest(
        @NotBlank String content,
        Long modelConfigId
) {
}
