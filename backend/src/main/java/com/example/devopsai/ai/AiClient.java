package com.example.devopsai.ai;

import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.ErrorCode;
import com.example.devopsai.common.security.SensitiveDataMasker;
import com.example.devopsai.ai.vo.AiResponse;
import com.example.devopsai.model.dto.ResolvedModelConfig;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
/**
 * AiClient类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Component
public class AiClient {

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    /**
     * RestClient构建器。
     */
    private final RestClient.Builder restClientBuilder;
    /**
     * 创建AiClient实例。
     * @param restClientBuilder restClientBuilder参数。
     */

    public AiClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }
    /**
     * 执行createResponse处理逻辑。
     * @param modelConfig modelConfig参数。
     * @param systemPrompt systemPrompt参数。
     * @param userPrompt userPrompt参数。
     * @return 处理结果。
     */

    public AiResponse createResponse(ResolvedModelConfig modelConfig, String systemPrompt, String userPrompt) {
        var endpoint = normalizeBaseUrl(modelConfig.baseUrl()) + "/v1/responses";
        log.info("ai_request_start provider={} model={} endpoint={} timeoutSeconds={}",
                modelConfig.provider(), modelConfig.model(), endpoint, modelConfig.timeoutSeconds());
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(modelConfig.timeoutSeconds()));
        var client = restClientBuilder
                .clone()
                .baseUrl(endpoint)
                .requestFactory(requestFactory)
                .build();
        var requestBody = Map.of(
                "model", modelConfig.model(),
                "input", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", modelConfig.temperature(),
                "max_output_tokens", modelConfig.maxTokens(),
                "text", Map.of("format", jsonSchemaFormat())
        );
        try {
            var body = client.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + modelConfig.apiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "AI 响应为空");
            }
            var response = new AiResponse(
                    extractText(body),
                    body.toString(),
                    usage(body, "input_tokens"),
                    usage(body, "output_tokens")
            );
            log.info("ai_request_success provider={} model={} promptTokens={} completionTokens={} totalTokens={}",
                    modelConfig.provider(), modelConfig.model(),
                    response.promptTokens(), response.completionTokens(), response.totalTokens());
            return response;
        } catch (RestClientException exception) {
            log.warn("ai_request_failed provider={} model={} error={}",
                    modelConfig.provider(), modelConfig.model(),
                    SensitiveDataMasker.maskInline(exception.getMessage()));
            throw new BusinessException(ErrorCode.AI_PROVIDER_CALL_FAILED, "AI 调用失败，请稍后重试");
        }
    }
    /**
     * 执行jsonSchemaFormat处理逻辑。
     * @return 处理结果。
     */

    private Map<String, Object> jsonSchemaFormat() {
        var properties = new LinkedHashMap<String, Object>();
        properties.put("summary", Map.of("type", "string"));
        properties.put("possibleCauses", stringArraySchema());
        properties.put("checkSteps", stringArraySchema());
        properties.put("fixSteps", stringArraySchema());
        properties.put("commands", Map.of(
                "type", "array",
                "items", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "required", List.of("command", "description", "riskLevel", "warning"),
                        "properties", Map.of(
                                "command", Map.of("type", "string"),
                                "description", Map.of("type", "string"),
                                "riskLevel", riskLevelSchema(),
                                "warning", Map.of("type", "string")
                        )
                )
        ));
        properties.put("riskLevel", riskLevelSchema());
        properties.put("riskWarnings", stringArraySchema());
        properties.put("needRestart", Map.of("type", "boolean"));
        properties.put("dataRisk", Map.of("type", "boolean"));
        properties.put("prevention", Map.of("type", "string"));
        properties.put("needMoreInfo", stringArraySchema());
        return Map.of(
                "type", "json_schema",
                "name", "devops_diagnosis_response",
                "strict", true,
                "schema", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "required", List.of(
                                "summary",
                                "possibleCauses",
                                "checkSteps",
                                "fixSteps",
                                "commands",
                                "riskLevel",
                                "riskWarnings",
                                "needRestart",
                                "dataRisk",
                                "prevention",
                                "needMoreInfo"
                        ),
                        "properties", properties
                )
        );
    }
    /**
     * 执行stringArraySchema处理逻辑。
     * @return 处理结果。
     */

    private Map<String, Object> stringArraySchema() {
        return Map.of("type", "array", "items", Map.of("type", "string"));
    }
    /**
     * 执行riskLevelSchema处理逻辑。
     * @return 处理结果。
     */

    private Map<String, Object> riskLevelSchema() {
        return Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"));
    }
    /**
     * 执行extractText处理逻辑。
     * @param body body参数。
     * @return 处理结果。
     */

    private String extractText(JsonNode body) {
        if (StringUtils.hasText(body.path("output_text").asText(null))) {
            return body.path("output_text").asText();
        }
        var output = body.path("output");
        if (output.isArray()) {
            for (var item : output) {
                var content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (var part : content) {
                    var text = part.path("text").asText(null);
                    if (StringUtils.hasText(text)) {
                        return text;
                    }
                }
            }
        }
        throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "AI 响应缺少 output_text");
    }
    /**
     * 执行usage处理逻辑。
     * @param body body参数。
     * @param field field参数。
     * @return 处理结果。
     */

    private int usage(JsonNode body, String field) {
        return body.path("usage").path(field).asInt(0);
    }
    /**
     * 执行normalizeBaseUrl处理逻辑。
     * @param baseUrl baseUrl参数。
     * @return 处理结果。
     */

    private String normalizeBaseUrl(String baseUrl) {
        var value = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "https://api.nexustokenai.com";
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.endsWith("/v1")) {
            value = value.substring(0, value.length() - 3);
        }
        return value;
    }
}
