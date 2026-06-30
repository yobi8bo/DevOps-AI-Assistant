package com.example.devopsai.risk.vo;

import com.example.devopsai.diagnosis.vo.CommandSuggestion;
import java.util.List;

public record DetectionResult(
        List<CommandSuggestion> commands,
        String riskLevel,
        List<String> warnings
) {
}
