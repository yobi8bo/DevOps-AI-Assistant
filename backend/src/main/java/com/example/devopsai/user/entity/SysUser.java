package com.example.devopsai.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
/**
 * SysUser类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@TableName("sys_user")
public class SysUser {

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 登录用户名。
     */
    private String username;
    /**
     * 密码哈希值。
     */
    private String passwordHash;
    /**
     * 用户昵称。
     */
    private String nickname;
    /**
     * 用户邮箱。
     */
    private String email;
    /**
     * 用户手机号。
     */
    private String phone;
    /**
     * 业务状态。
     */
    private Integer status;
    /**
     * 最近登录时间。
     */
    private LocalDateTime lastLoginAt;
    /**
     * 逻辑删除标记。
     */
    private Integer deleted;
    /**
     * 获取主键ID。
     * @return 处理结果。
     */

    public Long getId() {
        return id;
    }
    /**
     * 设置主键ID。
     * @param id id参数。
     */

    public void setId(Long id) {
        this.id = id;
    }
    /**
     * 获取登录用户名。
     * @return 处理结果。
     */

    public String getUsername() {
        return username;
    }
    /**
     * 设置登录用户名。
     * @param username username参数。
     */

    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * 获取密码哈希值。
     * @return 处理结果。
     */

    public String getPasswordHash() {
        return passwordHash;
    }
    /**
     * 设置密码哈希值。
     * @param passwordHash passwordHash参数。
     */

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    /**
     * 获取用户昵称。
     * @return 处理结果。
     */

    public String getNickname() {
        return nickname;
    }
    /**
     * 设置用户昵称。
     * @param nickname nickname参数。
     */

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    /**
     * 获取用户邮箱。
     * @return 处理结果。
     */

    public String getEmail() {
        return email;
    }
    /**
     * 设置用户邮箱。
     * @param email email参数。
     */

    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * 获取用户手机号。
     * @return 处理结果。
     */

    public String getPhone() {
        return phone;
    }
    /**
     * 设置用户手机号。
     * @param phone phone参数。
     */

    public void setPhone(String phone) {
        this.phone = phone;
    }
    /**
     * 获取业务状态。
     * @return 处理结果。
     */

    public Integer getStatus() {
        return status;
    }
    /**
     * 设置业务状态。
     * @param status status参数。
     */

    public void setStatus(Integer status) {
        this.status = status;
    }
    /**
     * 获取最近登录时间。
     * @return 处理结果。
     */

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    /**
     * 设置最近登录时间。
     * @param lastLoginAt lastLoginAt参数。
     */

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    /**
     * 获取逻辑删除标记。
     * @return 处理结果。
     */

    public Integer getDeleted() {
        return deleted;
    }
    /**
     * 设置逻辑删除标记。
     * @param deleted deleted参数。
     */

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

