package com.example.devopsai.category;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.category.entity.FaultCategory;
import com.example.devopsai.category.mapper.FaultCategoryMapper;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.PageResponse;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FaultCategoryService {

    private final FaultCategoryMapper faultCategoryMapper;

    public FaultCategoryService(FaultCategoryMapper faultCategoryMapper) {
        this.faultCategoryMapper = faultCategoryMapper;
    }

    public PageResponse<CategorySummary> list(CategoryQuery query) {
        var wrapper = baseWrapper()
                .orderByAsc(FaultCategory::getSortOrder)
                .orderByAsc(FaultCategory::getId);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(FaultCategory::getCategoryCode, query.keyword())
                    .or()
                    .like(FaultCategory::getCategoryName, query.keyword())
                    .or()
                    .like(FaultCategory::getDescription, query.keyword()));
        }
        if (query.status() != null) {
            validateStatus(query.status());
            wrapper.eq(FaultCategory::getStatus, query.status());
        }
        var page = faultCategoryMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public List<CategoryOption> options() {
        return faultCategoryMapper.selectList(baseWrapper()
                        .eq(FaultCategory::getStatus, 1)
                        .orderByAsc(FaultCategory::getSortOrder)
                        .orderByAsc(FaultCategory::getId))
                .stream()
                .map(item -> new CategoryOption(item.getId(), item.getCategoryCode(), item.getCategoryName()))
                .toList();
    }

    public CategorySummary get(Long id) {
        return toSummary(selectExisting(id));
    }

    @Transactional
    public CategorySummary create(CreateCategoryRequest request, Long operatorId) {
        if (!StringUtils.hasText(request.categoryCode())) {
            throw new BusinessException(400, "分类编码不能为空");
        }
        if (!StringUtils.hasText(request.categoryName())) {
            throw new BusinessException(400, "分类名称不能为空");
        }
        ensureCodeAvailable(request.categoryCode(), null);
        var entity = new FaultCategory();
        entity.setCategoryCode(request.categoryCode().trim());
        entity.setCategoryName(request.categoryName().trim());
        entity.setDescription(request.description());
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(request.status() == null ? 1 : request.status());
        validateStatus(entity.getStatus());
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        faultCategoryMapper.insert(entity);
        return get(entity.getId());
    }

    @Transactional
    public CategorySummary update(Long id, UpdateCategoryRequest request, Long operatorId) {
        var entity = selectExisting(id);
        if (StringUtils.hasText(request.categoryCode())) {
            ensureCodeAvailable(request.categoryCode(), id);
            entity.setCategoryCode(request.categoryCode().trim());
        }
        if (StringUtils.hasText(request.categoryName())) {
            entity.setCategoryName(request.categoryName().trim());
        }
        entity.setDescription(request.description());
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            validateStatus(request.status());
            entity.setStatus(request.status());
        }
        entity.setUpdatedBy(operatorId);
        faultCategoryMapper.updateById(entity);
        return get(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status, Long operatorId) {
        validateStatus(status);
        var entity = selectExisting(id);
        entity.setStatus(status);
        entity.setUpdatedBy(operatorId);
        faultCategoryMapper.updateById(entity);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        var entity = selectExisting(id);
        entity.setDeleted(1);
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedAt(LocalDateTime.now());
        faultCategoryMapper.updateById(entity);
    }

    private FaultCategory selectExisting(Long id) {
        var entity = faultCategoryMapper.selectOne(baseWrapper()
                .eq(FaultCategory::getId, id)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(404, "故障分类不存在");
        }
        return entity;
    }

    private LambdaQueryWrapper<FaultCategory> baseWrapper() {
        return new LambdaQueryWrapper<FaultCategory>().eq(FaultCategory::getDeleted, 0);
    }

    private void ensureCodeAvailable(String categoryCode, Long exceptId) {
        var wrapper = baseWrapper().eq(FaultCategory::getCategoryCode, categoryCode.trim());
        if (exceptId != null) {
            wrapper.ne(FaultCategory::getId, exceptId);
        }
        if (faultCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "分类编码已存在");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "分类状态参数错误");
        }
    }

    private CategorySummary toSummary(FaultCategory entity) {
        return new CategorySummary(
                entity.getId(),
                entity.getCategoryCode(),
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getSortOrder(),
                Integer.valueOf(1).equals(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public record CategoryQuery(String keyword, Integer status, long pageNum, long pageSize) {
    }

    public record CreateCategoryRequest(
            @NotBlank String categoryCode,
            @NotBlank String categoryName,
            String description,
            Integer sortOrder,
            Integer status
    ) {
    }

    public record UpdateCategoryRequest(
            String categoryCode,
            String categoryName,
            String description,
            Integer sortOrder,
            Integer status
    ) {
    }

    public record UpdateCategoryStatusRequest(Integer status) {
    }

    public record CategorySummary(
            Long id,
            String categoryCode,
            String categoryName,
            String description,
            Integer sortOrder,
            Boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record CategoryOption(
            Long id,
            String categoryCode,
            String categoryName
    ) {
    }
}
