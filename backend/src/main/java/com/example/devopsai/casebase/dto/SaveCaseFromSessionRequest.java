package com.example.devopsai.casebase.dto;

import java.util.List;

public record SaveCaseFromSessionRequest(
        String title,
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
