package com.example.devopsai.diagnosis.vo;

import java.util.List;

public record AnalyzeResponse(
        Long sessionId,
        Long messageId,
        Long resultId,
        String summary,
        List<String> possibleCauses,
        List<String> checkSteps,
        List<String> fixSteps,
        List<CommandSuggestion> commands,
        String riskLevel,
        List<String> riskWarnings,
        boolean needRestart,
        boolean dataRisk,
        String prevention,
        List<String> needMoreInfo
) {
}
