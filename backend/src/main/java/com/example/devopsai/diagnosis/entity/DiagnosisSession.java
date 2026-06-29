package com.example.devopsai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * DiagnosisSession会话实体，负责保存诊断会话数据。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_diagnosis_session")
public class DiagnosisSession {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 标题。
     */
    private String title;
    /**
     * 业务分类。
     */
    private String category;
    /**
     * 运行环境。
     */
    private String environment;
    /**
     * 操作系统信息。
     */
    private String osInfo;
    /**
     * 中间件信息。
     */
    private String middleware;
    /**
     * 服务类型。
     */
    private String serviceType;
    /**
     * 是否生产环境，1表示是。
     */
    private Integer isProduction;
    /**
     * 紧急程度。
     */
    private String urgencyLevel;
    /**
     * 业务状态。
     */
    private String status;
    /**
     * 用户ID。
     */
    private Long userId;
    /**
     * 最近消息时间。
     */
    private LocalDateTime lastMessageAt;
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
     * 获取标题。
     * @return 处理结果。
     */
    public String getTitle() { return title; }
    /**
     * 设置标题。
     * @param title title参数。
     */
    public void setTitle(String title) { this.title = title; }
    /**
     * 获取业务分类。
     * @return 处理结果。
     */
    public String getCategory() { return category; }
    /**
     * 设置业务分类。
     * @param category category参数。
     */
    public void setCategory(String category) { this.category = category; }
    /**
     * 获取运行环境。
     * @return 处理结果。
     */
    public String getEnvironment() { return environment; }
    /**
     * 设置运行环境。
     * @param environment environment参数。
     */
    public void setEnvironment(String environment) { this.environment = environment; }
    /**
     * 获取操作系统信息。
     * @return 处理结果。
     */
    public String getOsInfo() { return osInfo; }
    /**
     * 设置操作系统信息。
     * @param osInfo osInfo参数。
     */
    public void setOsInfo(String osInfo) { this.osInfo = osInfo; }
    /**
     * 获取中间件信息。
     * @return 处理结果。
     */
    public String getMiddleware() { return middleware; }
    /**
     * 设置中间件信息。
     * @param middleware middleware参数。
     */
    public void setMiddleware(String middleware) { this.middleware = middleware; }
    /**
     * 获取服务类型。
     * @return 处理结果。
     */
    public String getServiceType() { return serviceType; }
    /**
     * 设置服务类型。
     * @param serviceType serviceType参数。
     */
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    /**
     * 获取是否生产环境，1表示是。
     * @return 处理结果。
     */
    public Integer getIsProduction() { return isProduction; }
    /**
     * 设置是否生产环境，1表示是。
     * @param isProduction isProduction参数。
     */
    public void setIsProduction(Integer isProduction) { this.isProduction = isProduction; }
    /**
     * 获取紧急程度。
     * @return 处理结果。
     */
    public String getUrgencyLevel() { return urgencyLevel; }
    /**
     * 设置紧急程度。
     * @param urgencyLevel urgencyLevel参数。
     */
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    /**
     * 获取业务状态。
     * @return 处理结果。
     */
    public String getStatus() { return status; }
    /**
     * 设置业务状态。
     * @param status status参数。
     */
    public void setStatus(String status) { this.status = status; }
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
     * 获取最近消息时间。
     * @return 处理结果。
     */
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    /**
     * 设置最近消息时间。
     * @param lastMessageAt lastMessageAt参数。
     */
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
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

