package com.example.devopsai.risk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * RiskRule规则实体，负责保存风险识别规则。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_risk_rule")
public class RiskRule {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 风险规则名称。
     */
    private String ruleName;
    /**
     * 风险匹配表达式。
     */
    private String pattern;
    /**
     * 风险匹配表达式类型。
     */
    private String patternType;
    /**
     * 风险等级。
     */
    private String riskLevel;
    /**
     * 风险提示消息。
     */
    private String warningMessage;
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
     * 获取风险规则名称。
     * @return 处理结果。
     */
    public String getRuleName() { return ruleName; }
    /**
     * 设置风险规则名称。
     * @param ruleName ruleName参数。
     */
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    /**
     * 获取风险匹配表达式。
     * @return 处理结果。
     */
    public String getPattern() { return pattern; }
    /**
     * 设置风险匹配表达式。
     * @param pattern pattern参数。
     */
    public void setPattern(String pattern) { this.pattern = pattern; }
    /**
     * 获取风险匹配表达式类型。
     * @return 处理结果。
     */
    public String getPatternType() { return patternType; }
    /**
     * 设置风险匹配表达式类型。
     * @param patternType patternType参数。
     */
    public void setPatternType(String patternType) { this.patternType = patternType; }
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
     * 获取风险提示消息。
     * @return 处理结果。
     */
    public String getWarningMessage() { return warningMessage; }
    /**
     * 设置风险提示消息。
     * @param warningMessage warningMessage参数。
     */
    public void setWarningMessage(String warningMessage) { this.warningMessage = warningMessage; }
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
