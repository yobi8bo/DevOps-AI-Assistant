package com.example.devopsai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ops_diagnosis_result")
public class DiagnosisResult {

    private Long id;
    private Long sessionId;
    private Long messageId;
    private String summary;
    private String resultJson;
    private String rawResponse;
    private String riskLevel;
    private Integer needRestart;
    private Integer dataRisk;
    private Long modelConfigId;
    private Long promptTemplateId;
    private String promptVersion;
    private Long createdBy;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getNeedRestart() { return needRestart; }
    public void setNeedRestart(Integer needRestart) { this.needRestart = needRestart; }
    public Integer getDataRisk() { return dataRisk; }
    public void setDataRisk(Integer dataRisk) { this.dataRisk = dataRisk; }
    public Long getModelConfigId() { return modelConfigId; }
    public void setModelConfigId(Long modelConfigId) { this.modelConfigId = modelConfigId; }
    public Long getPromptTemplateId() { return promptTemplateId; }
    public void setPromptTemplateId(Long promptTemplateId) { this.promptTemplateId = promptTemplateId; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

