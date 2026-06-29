package com.example.devopsai.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * AiCallLog类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_ai_call_log")
public class AiCallLog {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 请求ID。
     */
    private String requestId;
    /**
     * 用户ID。
     */
    private Long userId;
    /**
     * 诊断会话ID。
     */
    private Long sessionId;
    /**
     * 模型配置ID。
     */
    private Long modelConfigId;
    /**
     * AI服务提供商。
     */
    private String provider;
    /**
     * 模型名称。
     */
    private String modelName;
    /**
     * 提示词Token数量。
     */
    private Integer promptTokens;
    /**
     * 补全Token数量。
     */
    private Integer completionTokens;
    /**
     * 总Token数量。
     */
    private Integer totalTokens;
    /**
     * 调用耗时，单位为毫秒。
     */
    private Integer latencyMs;
    /**
     * 调用成本。
     */
    private BigDecimal cost;
    /**
     * 是否调用成功，1表示成功。
     */
    private Integer success;
    /**
     * 错误码。
     */
    private String errorCode;
    /**
     * 错误消息。
     */
    private String errorMessage;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 获取主键ID。
     * @return 处理结果。
     */

    public Long getId() { return id; }
    /**
     * 设置主键ID。
     * @param id id参数。
     */
    public void setId(Long id) { this.id = id; }
    /**
     * 获取请求ID。
     * @return 处理结果。
     */
    public String getRequestId() { return requestId; }
    /**
     * 设置请求ID。
     * @param requestId requestId参数。
     */
    public void setRequestId(String requestId) { this.requestId = requestId; }
    /**
     * 获取用户ID。
     * @return 处理结果。
     */
    public Long getUserId() { return userId; }
    /**
     * 设置用户ID。
     * @param userId userId参数。
     */
    public void setUserId(Long userId) { this.userId = userId; }
    /**
     * 获取诊断会话ID。
     * @return 处理结果。
     */
    public Long getSessionId() { return sessionId; }
    /**
     * 设置诊断会话ID。
     * @param sessionId sessionId参数。
     */
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    /**
     * 获取模型配置ID。
     * @return 处理结果。
     */
    public Long getModelConfigId() { return modelConfigId; }
    /**
     * 设置模型配置ID。
     * @param modelConfigId modelConfigId参数。
     */
    public void setModelConfigId(Long modelConfigId) { this.modelConfigId = modelConfigId; }
    /**
     * 获取AI服务提供商。
     * @return 处理结果。
     */
    public String getProvider() { return provider; }
    /**
     * 设置AI服务提供商。
     * @param provider provider参数。
     */
    public void setProvider(String provider) { this.provider = provider; }
    /**
     * 获取模型名称。
     * @return 处理结果。
     */
    public String getModelName() { return modelName; }
    /**
     * 设置模型名称。
     * @param modelName modelName参数。
     */
    public void setModelName(String modelName) { this.modelName = modelName; }
    /**
     * 获取提示词Token数量。
     * @return 处理结果。
     */
    public Integer getPromptTokens() { return promptTokens; }
    /**
     * 设置提示词Token数量。
     * @param promptTokens promptTokens参数。
     */
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }
    /**
     * 获取补全Token数量。
     * @return 处理结果。
     */
    public Integer getCompletionTokens() { return completionTokens; }
    /**
     * 设置补全Token数量。
     * @param completionTokens completionTokens参数。
     */
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }
    /**
     * 获取总Token数量。
     * @return 处理结果。
     */
    public Integer getTotalTokens() { return totalTokens; }
    /**
     * 设置总Token数量。
     * @param totalTokens totalTokens参数。
     */
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
    /**
     * 获取调用耗时，单位为毫秒。
     * @return 处理结果。
     */
    public Integer getLatencyMs() { return latencyMs; }
    /**
     * 设置调用耗时，单位为毫秒。
     * @param latencyMs latencyMs参数。
     */
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }
    /**
     * 获取调用成本。
     * @return 处理结果。
     */
    public BigDecimal getCost() { return cost; }
    /**
     * 设置调用成本。
     * @param cost cost参数。
     */
    public void setCost(BigDecimal cost) { this.cost = cost; }
    /**
     * 获取是否调用成功，1表示成功。
     * @return 处理结果。
     */
    public Integer getSuccess() { return success; }
    /**
     * 设置是否调用成功，1表示成功。
     * @param success success参数。
     */
    public void setSuccess(Integer success) { this.success = success; }
    /**
     * 获取错误码。
     * @return 处理结果。
     */
    public String getErrorCode() { return errorCode; }
    /**
     * 设置错误码。
     * @param errorCode errorCode参数。
     */
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    /**
     * 获取错误消息。
     * @return 处理结果。
     */
    public String getErrorMessage() { return errorMessage; }
    /**
     * 设置错误消息。
     * @param errorMessage errorMessage参数。
     */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    /**
     * 获取创建时间。
     * @return 处理结果。
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * 设置创建时间。
     * @param createdAt createdAt参数。
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
