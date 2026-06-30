package com.example.devopsai.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.ErrorCode;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.user.dto.CreateUserRequest;
import com.example.devopsai.user.dto.ResetPasswordRequest;
import com.example.devopsai.user.dto.UpdateUserRequest;
import com.example.devopsai.user.dto.UpdateUserRolesRequest;
import com.example.devopsai.user.dto.UserQuery;
import com.example.devopsai.user.entity.SysUser;
import com.example.devopsai.user.mapper.SysUserMapper;
import com.example.devopsai.user.vo.UserDetail;
import com.example.devopsai.user.vo.UserSummary;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<UserSummary> list(UserQuery query) {
        var wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getUpdatedAt);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.keyword())
                    .or()
                    .like(SysUser::getNickname, query.keyword())
                    .or()
                    .like(SysUser::getEmail, query.keyword())
                    .or()
                    .like(SysUser::getPhone, query.keyword()));
        }
        if (query.status() != null) {
            validateStatus(query.status());
            wrapper.eq(SysUser::getStatus, query.status());
        }
        var page = userMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public UserDetail get(Long id) {
        return toDetail(selectExisting(id));
    }

    @Transactional
    public UserDetail create(CreateUserRequest request, Long operatorId) {
        if (!StringUtils.hasText(request.username())) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "用户名不能为空");
        }
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "密码不能为空");
        }
        ensureUsernameAvailable(request.username(), null);
        var user = new SysUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setStatus(request.status() == null ? 1 : request.status());
        validateStatus(user.getStatus());
        user.setCreatedBy(operatorId);
        user.setUpdatedBy(operatorId);
        userMapper.insert(user);
        replaceRoles(user.getId(), request.roleIds());
        return toDetail(selectExisting(user.getId()));
    }

    @Transactional
    public UserDetail update(Long id, UpdateUserRequest request, Long operatorId) {
        var user = selectExisting(id);
        if (StringUtils.hasText(request.username())) {
            ensureUsernameAvailable(request.username(), id);
            user.setUsername(request.username());
        }
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        if (request.status() != null) {
            validateStatus(request.status());
            user.setStatus(request.status());
        }
        user.setUpdatedBy(operatorId);
        userMapper.updateById(user);
        return toDetail(selectExisting(id));
    }

    @Transactional
    public void updateStatus(Long id, Integer status, Long operatorId) {
        validateStatus(status);
        var user = selectExisting(id);
        user.setStatus(status);
        user.setUpdatedBy(operatorId);
        userMapper.updateById(user);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request, Long operatorId) {
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "新密码不能为空");
        }
        var user = selectExisting(id);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setUpdatedBy(operatorId);
        userMapper.updateById(user);
    }

    @Transactional
    public void updateRoles(Long id, UpdateUserRolesRequest request) {
        selectExisting(id);
        replaceRoles(id, request == null ? List.of() : request.roleIds());
    }

    private SysUser selectExisting(Long id) {
        var user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username, Long exceptId) {
        var wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0);
        if (exceptId != null) {
            wrapper.ne(SysUser::getId, exceptId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.COMMON_CONFLICT, "用户名已存在");
        }
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        userMapper.deleteUserRoles(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (var roleId : roleIds.stream().distinct().toList()) {
            if (roleId == null || userMapper.countRoleById(roleId) == 0) {
                throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "角色不存在：" + roleId);
            }
            userMapper.insertUserRole(userId, roleId);
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "用户状态参数错误");
        }
    }

    private UserSummary toSummary(SysUser user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                Integer.valueOf(1).equals(user.getStatus()),
                userMapper.selectRoleCodes(user.getId()),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserDetail toDetail(SysUser user) {
        var summary = toSummary(user);
        return new UserDetail(
                summary.id(),
                summary.username(),
                summary.nickname(),
                summary.email(),
                summary.phone(),
                summary.enabled(),
                summary.roles(),
                userMapper.selectRoleIds(user.getId()),
                summary.lastLoginAt(),
                summary.createdAt(),
                summary.updatedAt()
        );
    }

}
