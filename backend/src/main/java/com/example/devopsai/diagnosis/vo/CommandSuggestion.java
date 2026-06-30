package com.example.devopsai.diagnosis.vo;

public record CommandSuggestion(
        String command,
        String description,
        String riskLevel,
        String warning
) {
}
