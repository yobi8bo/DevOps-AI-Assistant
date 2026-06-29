package com.example.devopsai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.devopsai.user.entity.SysUser;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
}

