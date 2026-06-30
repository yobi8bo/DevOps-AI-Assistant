package com.example.devopsai.diagnosis.vo;

import java.time.LocalDateTime;

public record ResultItem(
        Long id,
        String summary,
        String riskLevel,
        String resultJson,
        Long modelConfigId,
        Long promptTemplateId,
        String promptVersion,
        LocalDateTime createdAt
) {
}
