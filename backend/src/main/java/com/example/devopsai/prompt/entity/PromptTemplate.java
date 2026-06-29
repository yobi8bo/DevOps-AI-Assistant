package com.example.devopsai.prompt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * PromptTemplate类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("ops_prompt_template")
public class PromptTemplate {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 名称。
     */
    private String name;
    /**
     * 业务分类。
     */
    private String category;
    /**
     * 内容。
     */
    private String content;
    /**
     * 版本号。
     */
    private String version;
    /**
     * 是否默认，1表示默认。
     */
    private Integer isDefault;
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
     * 获取名称。
     * @return 处理结果。
     */
    public String getName() { return name; }
    /**
     * 设置名称。
     * @param name name参数。
     */
    public void setName(String name) { this.name = name; }
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
     * 获取版本号。
     * @return 处理结果。
     */
    public String getVersion() { return version; }
    /**
     * 设置版本号。
     * @param version version参数。
     */
    public void setVersion(String version) { this.version = version; }
    /**
     * 获取是否默认，1表示默认。
     * @return 处理结果。
     */
    public Integer getIsDefault() { return isDefault; }
    /**
     * 设置是否默认，1表示默认。
     * @param isDefault isDefault参数。
     */
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
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
