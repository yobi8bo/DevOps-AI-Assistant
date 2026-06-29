package com.example.devopsai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * DiagnosisMessage消息实体，负责保存诊断消息数据。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_diagnosis_message")
public class DiagnosisMessage {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 诊断会话ID。
     */
    private Long sessionId;
    /**
     * 消息角色。
     */
    private String role;
    /**
     * 内容。
     */
    private String content;
    /**
     * 脱敏后的消息内容。
     */
    private String contentSanitized;
    /**
     * 消息Token数量。
     */
    private Integer tokenCount;
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
     * 获取消息角色。
     * @return 处理结果。
     */
    public String getRole() { return role; }
    /**
     * 设置消息角色。
     * @param role role参数。
     */
    public void setRole(String role) { this.role = role; }
    /**
     * 获取内容。
     * @return 处理结果。
     */
    public String getContent() { return content; }
    /**
     * 设置内容。
     * @param content content参数。
     */
    public void setContent(String content) { this.content = content; }
    /**
     * 获取脱敏后的消息内容。
     * @return 处理结果。
     */
    public String getContentSanitized() { return contentSanitized; }
    /**
     * 设置脱敏后的消息内容。
     * @param contentSanitized contentSanitized参数。
     */
    public void setContentSanitized(String contentSanitized) { this.contentSanitized = contentSanitized; }
    /**
     * 获取消息Token数量。
     * @return 处理结果。
     */
    public Integer getTokenCount() { return tokenCount; }
    /**
     * 设置消息Token数量。
     * @param tokenCount tokenCount参数。
     */
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }
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

