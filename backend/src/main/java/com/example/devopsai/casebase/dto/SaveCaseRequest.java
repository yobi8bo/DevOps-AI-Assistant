package com.example.devopsai.casebase.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SaveCaseRequest(
        Long sourceSessionId,
        @NotBlank String title,
        String category,
        String environment,
        String symptom,
        String logContent,
        String causeAnalysis,
        String solution,
        String prevention,
        Object commands,
        List<String> tags,
        String status
) {
}
