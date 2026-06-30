package com.example.devopsai.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.role.entity.SysPermission;
import com.example.devopsai.role.entity.SysRole;
import com.example.devopsai.role.mapper.PermissionMapper;
import com.example.devopsai.role.mapper.RoleMapper;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleService(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public PageResponse<RoleSummary> list(RoleQuery query) {
        var wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .orderByDesc(SysRole::getUpdatedAt);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, query.keyword())
                    .or()
                    .like(SysRole::getRoleName, query.keyword())
                    .or()
                    .like(SysRole::getDescription, query.keyword()));
        }
        if (query.status() != null) {
            validateStatus(query.status());
            wrapper.eq(SysRole::getStatus, query.status());
        }
        var page = roleMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public RoleDetail get(Long id) {
        return toDetail(selectExisting(id));
    }

    @Transactional
    public RoleDetail create(CreateRoleRequest request, Long operatorId) {
        if (!StringUtils.hasText(request.roleCode())) {
            throw new BusinessException(400, "角色编码不能为空");
        }
        if (!StringUtils.hasText(request.roleName())) {
            throw new BusinessException(400, "角色名称不能为空");
        }
        ensureRoleCodeAvailable(request.roleCode(), null);
        var role = new SysRole();
        role.setRoleCode(request.roleCode());
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        role.setStatus(request.status() == null ? 1 : request.status());
        validateStatus(role.getStatus());
        role.setCreatedBy(operatorId);
        role.setUpdatedBy(operatorId);
        roleMapper.insert(role);
        replacePermissions(role.getId(), request.permissionIds());
        return toDetail(selectExisting(role.getId()));
    }

    @Transactional
    public RoleDetail update(Long id, UpdateRoleRequest request, Long operatorId) {
        var role = selectExisting(id);
        if (StringUtils.hasText(request.roleCode())) {
            ensureRoleCodeAvailable(request.roleCode(), id);
            role.setRoleCode(request.roleCode());
        }
        if (StringUtils.hasText(request.roleName())) {
            role.setRoleName(request.roleName());
        }
        role.setDescription(request.description());
        if (request.status() != null) {
            validateStatus(request.status());
            role.setStatus(request.status());
        }
        role.setUpdatedBy(operatorId);
        roleMapper.updateById(role);
        return toDetail(selectExisting(id));
    }

    @Transactional
    public void updateStatus(Long id, Integer status, Long operatorId) {
        validateStatus(status);
        var role = selectExisting(id);
        role.setStatus(status);
        role.setUpdatedBy(operatorId);
        roleMapper.updateById(role);
    }

    @Transactional
    public void updatePermissions(Long id, UpdateRolePermissionsRequest request) {
        selectExisting(id);
        replacePermissions(id, request == null ? List.of() : request.permissionIds());
    }

    public List<PermissionSummary> listPermissions(String keyword) {
        var wrapper = new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getPermissionCode);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysPermission::getPermissionCode, keyword)
                    .or()
                    .like(SysPermission::getPermissionName, keyword)
                    .or()
                    .like(SysPermission::getDescription, keyword));
        }
        return permissionMapper.selectList(wrapper).stream().map(this::toPermissionSummary).toList();
    }

    private SysRole selectExisting(Long id) {
        var role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, id)
                .eq(SysRole::getDeleted, 0)
                .last("LIMIT 1"));
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    private void ensureRoleCodeAvailable(String roleCode, Long exceptId) {
        var wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, 0);
        if (exceptId != null) {
            wrapper.ne(SysRole::getId, exceptId);
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "角色编码已存在");
        }
    }

    private void replacePermissions(Long roleId, List<Long> permissionIds) {
        roleMapper.deleteRolePermissions(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (var permissionId : permissionIds.stream().distinct().toList()) {
            if (permissionId == null || roleMapper.countPermissionById(permissionId) == 0) {
                throw new BusinessException(400, "权限不存在：" + permissionId);
            }
            roleMapper.insertRolePermission(roleId, permissionId);
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "角色状态参数错误");
        }
    }

    private RoleSummary toSummary(SysRole role) {
        return new RoleSummary(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                Integer.valueOf(1).equals(role.getStatus()),
                roleMapper.selectPermissionCodes(role.getId()),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private RoleDetail toDetail(SysRole role) {
        var summary = toSummary(role);
        return new RoleDetail(
                summary.id(),
                summary.roleCode(),
                summary.roleName(),
                summary.description(),
                summary.enabled(),
                summary.permissions(),
                roleMapper.selectPermissionIds(role.getId()),
                summary.createdAt(),
                summary.updatedAt()
        );
    }

    private PermissionSummary toPermissionSummary(SysPermission permission) {
        return new PermissionSummary(
                permission.getId(),
                permission.getPermissionCode(),
                permission.getPermissionName(),
                permission.getDescription()
        );
    }

    public record RoleQuery(String keyword, Integer status, long pageNum, long pageSize) {
    }

    public record CreateRoleRequest(
            @NotBlank String roleCode,
            @NotBlank String roleName,
            String description,
            Integer status,
            List<Long> permissionIds
    ) {
    }

    public record UpdateRoleRequest(
            String roleCode,
            String roleName,
            String description,
            Integer status
    ) {
    }

    public record UpdateRoleStatusRequest(Integer status) {
    }

    public record UpdateRolePermissionsRequest(List<Long> permissionIds) {
    }

    public record RoleSummary(
            Long id,
            String roleCode,
            String roleName,
            String description,
            Boolean enabled,
            List<String> permissions,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record RoleDetail(
            Long id,
            String roleCode,
            String roleName,
            String description,
            Boolean enabled,
            List<String> permissions,
            List<Long> permissionIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record PermissionSummary(
            Long id,
            String permissionCode,
            String permissionName,
            String description
    ) {
    }
}
