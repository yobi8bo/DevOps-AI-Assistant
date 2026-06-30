package com.example.devopsai.casebase.vo;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;

public record CaseDetail(
        Long id,
        Long sourceSessionId,
        String title,
        String category,
        String environment,
        String symptom,
        String logContent,
        String causeAnalysis,
        String solution,
        String prevention,
        JsonNode commands,
        List<String> tags,
        String status,
        Long createdBy,
        Long reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
