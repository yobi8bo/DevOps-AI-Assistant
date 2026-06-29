package com.example.devopsai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * DiagnosisResult结果实体，负责保存诊断结果数据。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_diagnosis_result")
public class DiagnosisResult {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 诊断会话ID。
     */
    private Long sessionId;
    /**
     * 诊断消息ID。
     */
    private Long messageId;
    /**
     * 摘要。
     */
    private String summary;
    /**
     * 结构化结果JSON。
     */
    private String resultJson;
    /**
     * AI原始响应。
     */
    private String rawResponse;
    /**
     * 风险等级。
     */
    private String riskLevel;
    /**
     * 是否需要重启，1表示需要。
     */
    private Integer needRestart;
    /**
     * 是否存在数据风险，1表示存在。
     */
    private Integer dataRisk;
    /**
     * 模型配置ID。
     */
    private Long modelConfigId;
    /**
     * 提示词模板ID。
     */
    private Long promptTemplateId;
    /**
     * 提示词版本。
     */
    private String promptVersion;
    /**
     * 创建人ID。
     */
    private Long createdBy;
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
     * 获取诊断消息ID。
     * @return 处理结果。
     */
    public Long getMessageId() { return messageId; }
    /**
     * 设置诊断消息ID。
     * @param messageId messageId参数。
     */
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    /**
     * 获取摘要。
     * @return 处理结果。
     */
    public String getSummary() { return summary; }
    /**
     * 设置摘要。
     * @param summary summary参数。
     */
    public void setSummary(String summary) { this.summary = summary; }
    /**
     * 获取结构化结果JSON。
     * @return 处理结果。
     */
    public String getResultJson() { return resultJson; }
    /**
     * 设置结构化结果JSON。
     * @param resultJson resultJson参数。
     */
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    /**
     * 获取AI原始响应。
     * @return 处理结果。
     */
    public String getRawResponse() { return rawResponse; }
    /**
     * 设置AI原始响应。
     * @param rawResponse rawResponse参数。
     */
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    /**
     * 获取风险等级。
     * @return 处理结果。
     */
    public String getRiskLevel() { return riskLevel; }
    /**
     * 设置风险等级。
     * @param riskLevel riskLevel参数。
     */
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    /**
     * 获取是否需要重启，1表示需要。
     * @return 处理结果。
     */
    public Integer getNeedRestart() { return needRestart; }
    /**
     * 设置是否需要重启，1表示需要。
     * @param needRestart needRestart参数。
     */
    public void setNeedRestart(Integer needRestart) { this.needRestart = needRestart; }
    /**
     * 获取是否存在数据风险，1表示存在。
     * @return 处理结果。
     */
    public Integer getDataRisk() { return dataRisk; }
    /**
     * 设置是否存在数据风险，1表示存在。
     * @param dataRisk dataRisk参数。
     */
    public void setDataRisk(Integer dataRisk) { this.dataRisk = dataRisk; }
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
     * 获取提示词模板ID。
     * @return 处理结果。
     */
    public Long getPromptTemplateId() { return promptTemplateId; }
    /**
     * 设置提示词模板ID。
     * @param promptTemplateId promptTemplateId参数。
     */
    public void setPromptTemplateId(Long promptTemplateId) { this.promptTemplateId = promptTemplateId; }
    /**
     * 获取提示词版本。
     * @return 处理结果。
     */
    public String getPromptVersion() { return promptVersion; }
    /**
     * 设置提示词版本。
     * @param promptVersion promptVersion参数。
     */
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
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

