package com.example.devopsai.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.devopsai.role.entity.SysRole;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIds(@Param("roleId") Long roleId);

    @Select("""
            SELECT p.permission_code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
            ORDER BY p.permission_code ASC
            """)
    List<String> selectPermissionCodes(@Param("roleId") Long roleId);

    @Select("SELECT COUNT(1) FROM sys_permission WHERE id = #{permissionId}")
    int countPermissionById(@Param("permissionId") Long permissionId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
