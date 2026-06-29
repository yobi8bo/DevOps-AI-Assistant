package com.example.devopsai.prompt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeRequest;
import com.example.devopsai.prompt.entity.PromptTemplate;
import com.example.devopsai.prompt.mapper.PromptTemplateMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PromptTemplateService {

    private final PromptTemplateMapper promptTemplateMapper;

    public PromptTemplateService(PromptTemplateMapper promptTemplateMapper) {
        this.promptTemplateMapper = promptTemplateMapper;
    }

    public PageResponse<PromptTemplateSummary> list(String keyword, String category, Integer status, long pageNum, long pageSize) {
        var wrapper = new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getDeleted, 0)
                .orderByDesc(PromptTemplate::getIsDefault)
                .orderByDesc(PromptTemplate::getUpdatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PromptTemplate::getName, keyword)
                    .or()
                    .like(PromptTemplate::getContent, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(PromptTemplate::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(PromptTemplate::getStatus, status);
        }
        IPage<PromptTemplate> page = promptTemplateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public PromptTemplateDetail get(Long id) {
        return toDetail(selectExisting(id));
    }

    @Transactional
    public PromptTemplateDetail create(SavePromptTemplateRequest request, Long userId) {
        var entity = new PromptTemplate();
        fill(entity, request);
        entity.setIsDefault(Boolean.TRUE.equals(request.defaultTemplate()) ? 1 : 0);
        entity.setStatus(request.status() == null ? 1 : request.status());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        if (Integer.valueOf(1).equals(entity.getIsDefault())) {
            clearDefaultForCategory(entity.getCategory(), null);
        }
        promptTemplateMapper.insert(entity);
        return toDetail(entity);
    }

    @Transactional
    public PromptTemplateDetail update(Long id, SavePromptTemplateRequest request, Long userId) {
        var entity = selectExisting(id);
        fill(entity, request);
        if (request.defaultTemplate() != null) {
            entity.setIsDefault(request.defaultTemplate() ? 1 : 0);
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setUpdatedBy(userId);
        if (Integer.valueOf(1).equals(entity.getIsDefault())) {
            clearDefaultForCategory(entity.getCategory(), id);
        }
        promptTemplateMapper.updateById(entity);
        return toDetail(selectExisting(id));
    }

    @Transactional
    public void updateStatus(Long id, Integer status, Long userId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态参数错误");
        }
        var entity = selectExisting(id);
        entity.setStatus(status);
        entity.setUpdatedBy(userId);
        promptTemplateMapper.updateById(entity);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        var entity = selectExisting(id);
        if (!Integer.valueOf(1).equals(entity.getStatus())) {
            throw new BusinessException(400, "停用的模板不能设为默认");
        }
        clearDefaultForCategory(entity.getCategory(), id);
        entity.setIsDefault(1);
        entity.setUpdatedBy(userId);
        promptTemplateMapper.updateById(entity);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        var entity = selectExisting(id);
        entity.setDeleted(1);
        entity.setUpdatedBy(userId);
        promptTemplateMapper.updateById(entity);
    }

    public RenderedPrompt renderForDiagnosis(AnalyzeRequest request) {
        var template = selectForCategory(request.category());
        if (template == null) {
            return new RenderedPrompt(null, null, defaultPrompt(request));
        }
        return new RenderedPrompt(template.getId(), template.getVersion(), render(template.getContent(), request));
    }

    private PromptTemplate selectForCategory(String category) {
        if (StringUtils.hasText(category)) {
            var categoryTemplate = promptTemplateMapper.selectOne(new LambdaQueryWrapper<PromptTemplate>()
                    .eq(PromptTemplate::getCategory, category)
                    .eq(PromptTemplate::getIsDefault, 1)
                    .eq(PromptTemplate::getStatus, 1)
                    .eq(PromptTemplate::getDeleted, 0)
                    .orderByDesc(PromptTemplate::getUpdatedAt)
                    .last("LIMIT 1"));
            if (categoryTemplate != null) {
                return categoryTemplate;
            }
        }
        return promptTemplateMapper.selectOne(new LambdaQueryWrapper<PromptTemplate>()
                .and(w -> w.isNull(PromptTemplate::getCategory).or().eq(PromptTemplate::getCategory, ""))
                .eq(PromptTemplate::getIsDefault, 1)
                .eq(PromptTemplate::getStatus, 1)
                .eq(PromptTemplate::getDeleted, 0)
                .orderByDesc(PromptTemplate::getUpdatedAt)
                .last("LIMIT 1"));
    }

    private String render(String content, AnalyzeRequest request) {
        var values = Map.ofEntries(
                Map.entry("title", nullToDash(request.title())),
                Map.entry("category", nullToDash(request.category())),
                Map.entry("environment", nullToDash(request.environment())),
                Map.entry("osInfo", nullToDash(request.osInfo())),
                Map.entry("middleware", nullToDash(request.middleware())),
                Map.entry("serviceType", nullToDash(request.serviceType())),
                Map.entry("isProduction", Boolean.TRUE.equals(request.isProduction()) ? "是" : "否"),
                Map.entry("urgencyLevel", nullToDash(request.urgencyLevel())),
                Map.entry("description", nullToDash(request.description())),
                Map.entry("logContent", nullToDash(request.logContent())),
                Map.entry("commandOutput", nullToDash(request.commandOutput()))
        );
        var rendered = content;
        for (var entry : values.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered;
    }

    private String defaultPrompt(AnalyzeRequest request) {
        return """
                请分析以下运维故障，并返回结构化 JSON。

                标题：%s
                故障类型：%s
                运行环境：%s
                操作系统：%s
                中间件：%s
                服务类型：%s
                是否生产环境：%s
                紧急程度：%s

                故障描述：
                %s

                日志内容：
                %s

                命令输出：
                %s
                """.formatted(
                nullToDash(request.title()),
                nullToDash(request.category()),
                nullToDash(request.environment()),
                nullToDash(request.osInfo()),
                nullToDash(request.middleware()),
                nullToDash(request.serviceType()),
                Boolean.TRUE.equals(request.isProduction()) ? "是" : "否",
                nullToDash(request.urgencyLevel()),
                nullToDash(request.description()),
                nullToDash(request.logContent()),
                nullToDash(request.commandOutput())
        );
    }

    private PromptTemplate selectExisting(Long id) {
        var entity = promptTemplateMapper.selectOne(new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getId, id)
                .eq(PromptTemplate::getDeleted, 0)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(404, "Prompt 模板不存在");
        }
        return entity;
    }

    private void fill(PromptTemplate entity, SavePromptTemplateRequest request) {
        if (!StringUtils.hasText(request.name())) {
            throw new BusinessException(400, "模板名称不能为空");
        }
        if (!StringUtils.hasText(request.content())) {
            throw new BusinessException(400, "模板内容不能为空");
        }
        entity.setName(request.name());
        entity.setCategory(request.category());
        entity.setContent(request.content());
        entity.setVersion(StringUtils.hasText(request.version()) ? request.version() : "1.0.0");
    }

    private void clearDefaultForCategory(String category, Long exceptId) {
        var wrapper = new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getIsDefault, 1)
                .eq(PromptTemplate::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(PromptTemplate::getCategory, category);
        } else {
            wrapper.and(w -> w.isNull(PromptTemplate::getCategory).or().eq(PromptTemplate::getCategory, ""));
        }
        if (exceptId != null) {
            wrapper.ne(PromptTemplate::getId, exceptId);
        }
        promptTemplateMapper.selectList(wrapper).forEach(item -> {
            item.setIsDefault(0);
            item.setUpdatedAt(LocalDateTime.now());
            promptTemplateMapper.updateById(item);
        });
    }

    private PromptTemplateSummary toSummary(PromptTemplate entity) {
        return new PromptTemplateSummary(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getVersion(),
                Integer.valueOf(1).equals(entity.getIsDefault()),
                Integer.valueOf(1).equals(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PromptTemplateDetail toDetail(PromptTemplate entity) {
        return new PromptTemplateDetail(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getContent(),
                entity.getVersion(),
                Integer.valueOf(1).equals(entity.getIsDefault()),
                Integer.valueOf(1).equals(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    public record RenderedPrompt(Long templateId, String version, String content) {
    }

    public record SavePromptTemplateRequest(
            String name,
            String category,
            String content,
            String version,
            Boolean defaultTemplate,
            Integer status
    ) {
    }

    public record PromptTemplateSummary(
            Long id,
            String name,
            String category,
            String version,
            Boolean defaultTemplate,
            Boolean enabled,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }

    public record PromptTemplateDetail(
            Long id,
            String name,
            String category,
            String content,
            String version,
            Boolean defaultTemplate,
            Boolean enabled,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
}
