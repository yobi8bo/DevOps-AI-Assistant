package com.example.devopsai.ai;

import com.example.devopsai.common.BusinessException;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeRequest;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeResponse;
import com.example.devopsai.diagnosis.DiagnosisController.CommandSuggestion;
import com.example.devopsai.knowledge.KnowledgeService;
import com.example.devopsai.model.ModelConfigService;
import com.example.devopsai.prompt.PromptTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
/**
 * AiDiagnosisService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class AiDiagnosisService {

    /**
     * 模型配置服务。
     */
    private final ModelConfigService modelConfigService;
    /**
     * 提示词模板服务。
     */
    private final PromptTemplateService promptTemplateService;
    /**
     * 知识库服务。
     */
    private final KnowledgeService knowledgeService;
    /**
     * AI客户端。
     */
    private final AiClient aiClient;
    /**
     * AI调用日志服务。
     */
    private final AiCallLogService aiCallLogService;
    /**
     * JSON序列化组件。
     */
    private final ObjectMapper objectMapper;
    /**
     * 创建AiDiagnosisService实例。
     * @param modelConfigService modelConfigService参数。
     * @param promptTemplateService promptTemplateService参数。
     * @param aiClient aiClient参数。
     * @param aiCallLogService aiCallLogService参数。
     * @param objectMapper objectMapper参数。
     */

    public AiDiagnosisService(
            ModelConfigService modelConfigService,
            PromptTemplateService promptTemplateService,
            KnowledgeService knowledgeService,
            AiClient aiClient,
            AiCallLogService aiCallLogService,
            ObjectMapper objectMapper
    ) {
        this.modelConfigService = modelConfigService;
        this.promptTemplateService = promptTemplateService;
        this.knowledgeService = knowledgeService;
        this.aiClient = aiClient;
        this.aiCallLogService = aiCallLogService;
        this.objectMapper = objectMapper;
    }
    /**
     * 执行智能诊断分析。
     * @param request request参数。
     * @param sessionId sessionId参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

    public DiagnosisAiResult analyze(AnalyzeRequest request, Long sessionId, Long userId) {
        var modelConfig = modelConfigService.resolve(request.modelConfigId());
        var renderedPrompt = promptTemplateService.renderForDiagnosis(request);
        var userPrompt = appendKnowledgeContext(renderedPrompt.content(), knowledgeService.buildRelevantKnowledgeContext(request));
        var requestId = UUID.randomUUID().toString();
        var startedAt = Instant.now();
        try {
            var aiResponse = aiClient.createResponse(modelConfig, systemPrompt(), userPrompt);
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
    /**
     * 转换业务数据视图。
     * @param content content参数。
     * @param sessionId sessionId参数。
     * @return 处理结果。
     */

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

    private String appendKnowledgeContext(String prompt, String knowledgeContext) {
        if (knowledgeContext == null || knowledgeContext.isBlank()) {
            return prompt;
        }
        return prompt + """


                以下是系统检索到的企业内部知识库片段。请只把它们作为辅助参考；如果与用户输入冲突，以用户输入为准；不要在结果中暴露知识库内部来源字段。

                相关知识库：
                %s
                """.formatted(knowledgeContext);
    }
    /**
     * 执行systemPrompt处理逻辑。
     * @return 处理结果。
     */

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
    /**
     * 执行stripJsonFence处理逻辑。
     * @param content content参数。
     * @return 处理结果。
     */

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
    /**
     * 执行normalizeRiskLevel处理逻辑。
     * @param value value参数。
     * @return 处理结果。
     */

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
    /**
     * 执行nullToEmpty处理逻辑。
     * @param value value参数。
     * @return 处理结果。
     */

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
    /**
     * 执行safeList处理逻辑。
     * @param values values参数。
     * @return 处理结果。
     */

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
    /**
     * DiagnosisAiResult结果实体，负责保存诊断结果数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record DiagnosisAiResult(
            AnalyzeResponse response,
            String rawResponse,
            Long modelConfigId,
            Long promptTemplateId,
            String promptVersion
    ) {
    }
    /**
     * 执行AiDiagnosisPayload业务逻辑。
     * @param summary summary参数。
     * @param possibleCauses possibleCauses参数。
     * @param checkSteps checkSteps参数。
     * @param fixSteps fixSteps参数。
     * @param commands commands参数。
     * @param riskLevel riskLevel参数。
     * @param riskWarnings riskWarnings参数。
     * @param needRestart needRestart参数。
     * @param dataRisk dataRisk参数。
     * @param prevention prevention参数。
     * @param needMoreInfo needMoreInfo参数。
     * @return 处理结果。
     */

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
