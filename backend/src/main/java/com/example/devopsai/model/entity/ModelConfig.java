package com.example.devopsai.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * ModelConfig配置类，负责声明对应模块的基础配置。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_model_config")
public class ModelConfig {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * AI服务提供商。
     */
    private String provider;
    /**
     * AI接口风格。
     */
    private String apiStyle;
    /**
     * 模型名称。
     */
    private String modelName;
    /**
     * AI接口基础地址。
     */
    private String apiBaseUrl;
    /**
     * 加密后的AI接口密钥。
     */
    private String apiKeyEncrypted;
    /**
     * AI响应最大Token数。
     */
    private Integer maxTokens;
    /**
     * AI采样温度。
     */
    private BigDecimal temperature;
    /**
     * AI请求超时时间，单位为秒。
     */
    private Integer timeoutSeconds;
    /**
     * 是否默认模型。
     */
    private Integer defaultModel;
    /**
     * 业务状态。
     */
    private Integer status;
    /**
     * 创建人ID。
     */
    private Long createdBy;
    /**
     * 更新人ID。
     */
    private Long updatedBy;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
    /**
     * 逻辑删除标记。
     */
    private Integer deleted;
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
     * 获取AI接口风格。
     * @return 处理结果。
     */
    public String getApiStyle() { return apiStyle; }
    /**
     * 设置AI接口风格。
     * @param apiStyle apiStyle参数。
     */
    public void setApiStyle(String apiStyle) { this.apiStyle = apiStyle; }
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
     * 获取AI接口基础地址。
     * @return 处理结果。
     */
    public String getApiBaseUrl() { return apiBaseUrl; }
    /**
     * 设置AI接口基础地址。
     * @param apiBaseUrl apiBaseUrl参数。
     */
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
    /**
     * 获取加密后的AI接口密钥。
     * @return 处理结果。
     */
    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    /**
     * 设置加密后的AI接口密钥。
     * @param apiKeyEncrypted apiKeyEncrypted参数。
     */
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }
    /**
     * 获取AI响应最大Token数。
     * @return 处理结果。
     */
    public Integer getMaxTokens() { return maxTokens; }
    /**
     * 设置AI响应最大Token数。
     * @param maxTokens maxTokens参数。
     */
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    /**
     * 获取AI采样温度。
     * @return 处理结果。
     */
    public BigDecimal getTemperature() { return temperature; }
    /**
     * 设置AI采样温度。
     * @param temperature temperature参数。
     */
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    /**
     * 获取AI请求超时时间，单位为秒。
     * @return 处理结果。
     */
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    /**
     * 设置AI请求超时时间，单位为秒。
     * @param timeoutSeconds timeoutSeconds参数。
     */
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    /**
     * 获取是否默认模型。
     * @return 处理结果。
     */
    public Integer getDefaultModel() { return defaultModel; }
    /**
     * 设置是否默认模型。
     * @param defaultModel defaultModel参数。
     */
    public void setDefaultModel(Integer defaultModel) { this.defaultModel = defaultModel; }
    /**
     * 获取业务状态。
     * @return 处理结果。
     */
    public Integer getStatus() { return status; }
    /**
     * 设置业务状态。
     * @param status status参数。
     */
    public void setStatus(Integer status) { this.status = status; }
    /**
     * 获取创建人ID。
     * @return 处理结果。
     */
    public Long getCreatedBy() { return createdBy; }
    /**
     * 设置创建人ID。
     * @param createdBy createdBy参数。
     */
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    /**
     * 获取更新人ID。
     * @return 处理结果。
     */
    public Long getUpdatedBy() { return updatedBy; }
    /**
     * 设置更新人ID。
     * @param updatedBy updatedBy参数。
     */
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
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
    /**
     * 获取更新时间。
     * @return 处理结果。
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    /**
     * 设置更新时间。
     * @param updatedAt updatedAt参数。
     */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    /**
     * 获取逻辑删除标记。
     * @return 处理结果。
     */
    public Integer getDeleted() { return deleted; }
    /**
     * 设置逻辑删除标记。
     * @param deleted deleted参数。
     */
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
