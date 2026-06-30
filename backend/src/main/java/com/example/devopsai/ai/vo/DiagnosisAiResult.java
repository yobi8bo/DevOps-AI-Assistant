package com.example.devopsai.ai.vo;

import com.example.devopsai.diagnosis.vo.AnalyzeResponse;

public record DiagnosisAiResult(
        AnalyzeResponse response,
        String rawResponse,
        Long modelConfigId,
        Long promptTemplateId,
        String promptVersion
) {
}
