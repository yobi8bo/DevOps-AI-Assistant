package com.example.devopsai.auth;

import com.example.devopsai.user.entity.SysUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
/**
 * AppUserPrincipal类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

public class AppUserPrincipal implements UserDetails {

    /**
     * 系统用户实体。
     */
    private final SysUser user;
    /**
     * 用户角色编码列表。
     */
    private final List<String> roles;
    /**
     * 用户权限编码列表。
     */
    private final List<String> permissions;
    /**
     * Spring Security授权信息。
     */
    private final List<GrantedAuthority> authorities;
    /**
     * 创建AppUserPrincipal实例。
     * @param user user参数。
     * @param roles roles参数。
     * @param permissions permissions参数。
     */

    public AppUserPrincipal(SysUser user, List<String> roles, List<String> permissions) {
        this.user = user;
        this.roles = List.copyOf(roles);
        this.permissions = List.copyOf(permissions);
        this.authorities = this.permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
    /**
     * 获取主键ID。
     * @return 处理结果。
     */

    public Long getId() {
        return user.getId();
    }
    /**
     * 获取用户昵称。
     * @return 处理结果。
     */

    public String getNickname() {
        return user.getNickname();
    }
    /**
     * 获取用户邮箱。
     * @return 处理结果。
     */

    public String getEmail() {
        return user.getEmail();
    }
    /**
     * 获取对应属性值。
     * @return 处理结果。
     */

    public List<String> getRoles() {
        return roles;
    }
    /**
     * 获取对应属性值。
     * @return 处理结果。
     */

    public List<String> getPermissions() {
        return permissions;
    }
    /**
     * 获取对应属性值。
     * @return 处理结果。
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    /**
     * 获取登录密码。
     * @return 处理结果。
     */

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    /**
     * 获取登录用户名。
     * @return 处理结果。
     */

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    /**
     * 执行isEnabled处理逻辑。
     * @return 处理结果。
     */

    @Override
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(user.getStatus());
    }
}

