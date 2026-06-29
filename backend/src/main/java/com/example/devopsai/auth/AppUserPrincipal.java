package com.example.devopsai.auth;

import com.example.devopsai.user.entity.SysUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUserPrincipal implements UserDetails {

    private final SysUser user;
    private final List<String> roles;
    private final List<String> permissions;
    private final List<GrantedAuthority> authorities;

    public AppUserPrincipal(SysUser user, List<String> roles, List<String> permissions) {
        this.user = user;
        this.roles = List.copyOf(roles);
        this.permissions = List.copyOf(permissions);
        this.authorities = this.permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public Long getId() {
        return user.getId();
    }

    public String getNickname() {
        return user.getNickname();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(user.getStatus());
    }
}

