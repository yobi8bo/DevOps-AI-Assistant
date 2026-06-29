package com.example.devopsai.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.user.entity.SysUser;
import com.example.devopsai.user.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService implements UserDetailsService {

    private final SysUserMapper userMapper;

    public UserAccountService(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

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

    public void recordLogin(Long userId) {
        userMapper.updateLastLoginAt(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return loadByUsername(username);
    }
}
