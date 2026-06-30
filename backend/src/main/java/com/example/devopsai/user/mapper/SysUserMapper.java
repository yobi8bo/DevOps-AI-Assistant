package com.example.devopsai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.devopsai.user.entity.SysUser;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
/**
 * SysUserMapper数据访问接口，负责对应实体的数据库操作。
 * 
 * @author zhang
 * @date 2026-06-29
 */

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("""
            SELECT r.role_code
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
              AND r.deleted = 0
            """)
    List<String> selectRoleCodes(@Param("userId") Long userId);

    @Select("""
            SELECT p.permission_code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_user_role ur ON ur.role_id = rp.role_id
            JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
              AND r.deleted = 0
            """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);

    @Update("UPDATE sys_user SET last_login_at = NOW() WHERE id = #{userId}")
    int updateLastLoginAt(@Param("userId") Long userId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIds(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM sys_role WHERE id = #{roleId} AND deleted = 0")
    int countRoleById(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
