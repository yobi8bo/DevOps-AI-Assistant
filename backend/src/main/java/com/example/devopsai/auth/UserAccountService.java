package com.example.devopsai.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.user.entity.SysUser;
import com.example.devopsai.user.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
/**
 * UserAccountService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class UserAccountService implements UserDetailsService {

    /**
     * 用户数据访问对象。
     */
    private final SysUserMapper userMapper;
    /**
     * 创建UserAccountService实例。
     * @param userMapper userMapper参数。
     */

    public UserAccountService(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }
    /**
     * 按用户名加载应用用户。
     * @param username username参数。
     * @return 处理结果。
     */

    public AppUserPrincipal loadByUsername(String username) {
        var user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "用户已被禁用");
        }
        return new AppUserPrincipal(user, userMapper.selectRoleCodes(user.getId()), userMapper.selectPermissionCodes(user.getId()));
    }
    /**
     * 记录用户最近登录时间。
     * @param userId userId参数。
     */

    public void recordLogin(Long userId) {
        userMapper.updateLastLoginAt(userId);
    }
    /**
     * 按用户名加载Spring Security用户。
     * @param username username参数。
     * @return 处理结果。
     */

    @Override
    public UserDetails loadUserByUsername(String username) {
        return loadByUsername(username);
    }
}
