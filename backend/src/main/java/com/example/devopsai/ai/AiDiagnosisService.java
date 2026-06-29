package com.example.devopsai.ai;

import com.example.devopsai.common.BusinessException;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeRequest;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeResponse;
import com.example.devopsai.diagnosis.DiagnosisController.CommandSuggestion;
import com.example.devopsai.model.ModelConfigService;
import com.example.devopsai.prompt.PromptTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AiDiagnosisService {

    private final ModelConfigService modelConfigService;
    private final PromptTemplateService promptTemplateService;
    private final AiClient aiClient;
    private final AiCallLogService aiCallLogService;
    private final ObjectMapper objectMapper;

    public AiDiagnosisService(
            ModelConfigService modelConfigService,
            PromptTemplateService promptTemplateService,
            AiClient aiClient,
            AiCallLogService aiCallLogService,
            ObjectMapper objectMapper
    ) {
        this.modelConfigService = modelConfigService;
        this.promptTemplateService = promptTemplateService;
        this.aiClient = aiClient;
        this.aiCallLogService = aiCallLogService;
        this.objectMapper = objectMapper;
    }

    public DiagnosisAiResult analyze(AnalyzeRequest request, Long sessionId, Long userId) {
        var modelConfig = modelConfigService.resolve(request.modelConfigId());
        var renderedPrompt = promptTemplateService.renderForDiagnosis(request);
        var requestId = UUID.randomUUID().toString();
        var startedAt = Instant.now();
        try {
            var aiResponse = aiClient.createResponse(modelConfig, systemPrompt(), renderedPrompt.content());
            var latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
            aiCallLogService.logSuccess(requestId, userId, sessionId, modelConfig, aiResponse, latencyMs);
            return new DiagnosisAiResult(
                    toAnalyzeResponse(aiResponse.text(), sessionId),
                    aiResponse.rawResponse(),
                    modelConfig.id(),
                    renderedPrompt.templateId(),
                    renderedPrompt.version()
            );
        } catch (BusinessException exception) {
            var latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
            aiCallLogService.logFailure(requestId, userId, sessionId, modelConfig, String.valueOf(exception.getCode()), exception.getMessage(), latencyMs);
            throw exception;
        } catch (RuntimeException exception) {
            var latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
            aiCallLogService.logFailure(requestId, userId, sessionId, modelConfig, "AI_RUNTIME_ERROR", exception.getMessage(), latencyMs);
            throw exception;
        }
    }

    private AnalyzeResponse toAnalyzeResponse(String content, Long sessionId) {
        try {
            var payload = objectMapper.readValue(stripJsonFence(content), AiDiagnosisPayload.class);
            return new AnalyzeResponse(
                    sessionId,
                    null,
                    null,
                    nullToEmpty(payload.summary()),
                    safeList(payload.possibleCauses()),
                    safeList(payload.checkSteps()),
                    safeList(payload.fixSteps()),
                    safeList(payload.commands()),
                    normalizeRiskLevel(payload.riskLevel()),
                    safeList(payload.riskWarnings()),
                    Boolean.TRUE.equals(payload.needRestart()),
                    Boolean.TRUE.equals(payload.dataRisk()),
                    nullToEmpty(payload.prevention()),
                    safeList(payload.needMoreInfo())
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(502, "AI 返回内容不是合法诊断 JSON");
        }
    }

    private String systemPrompt() {
        return """
                你是一个资深 DevOps 运维排障助手。请基于用户提供的故障描述、日志、命令输出和环境信息进行诊断。
                要求：
                1. 只输出符合 JSON Schema 的 JSON，不要输出 Markdown。
                2. 结论必须谨慎，不能编造用户没有提供的环境事实。
                3. 命令建议优先使用只读检查命令；涉及删除、格式化、重启、清理、防火墙变更、数据库写操作时必须标记 HIGH 或 CRITICAL，并写明风险提示。
                4. 如果信息不足，把需要补充的信息放入 needMoreInfo。
                5. riskLevel 只能是 LOW、MEDIUM、HIGH、CRITICAL。
                """;
    }

    private String stripJsonFence(String content) {
        var value = content == null ? "" : content.trim();
        if (value.startsWith("```json")) {
            value = value.substring(7).trim();
        } else if (value.startsWith("```")) {
            value = value.substring(3).trim();
        }
        if (value.endsWith("```")) {
            value = value.substring(0, value.length() - 3).trim();
        }
        return value;
    }

    private String normalizeRiskLevel(String value) {
        if (value == null) {
            return "LOW";
        }
        return switch (value.trim().toUpperCase()) {
            case "MEDIUM" -> "MEDIUM";
            case "HIGH" -> "HIGH";
            case "CRITICAL" -> "CRITICAL";
            default -> "LOW";
        };
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    public record DiagnosisAiResult(
            AnalyzeResponse response,
            String rawResponse,
            Long modelConfigId,
            Long promptTemplateId,
            String promptVersion
    ) {
    }

    private record AiDiagnosisPayload(
            String summary,
            List<String> possibleCauses,
            List<String> checkSteps,
            List<String> fixSteps,
            List<CommandSuggestion> commands,
            String riskLevel,
            List<String> riskWarnings,
            Boolean needRestart,
            Boolean dataRisk,
            String prevention,
            List<String> needMoreInfo
    ) {
    }
}
