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
/**
 * PromptTemplateService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class PromptTemplateService {

    /**
     * 提示词模板数据访问对象。
     */
    private final PromptTemplateMapper promptTemplateMapper;
    /**
     * 创建PromptTemplateService实例。
     * @param promptTemplateMapper promptTemplateMapper参数。
     */

    public PromptTemplateService(PromptTemplateMapper promptTemplateMapper) {
        this.promptTemplateMapper = promptTemplateMapper;
    }
    /**
     * 执行list处理逻辑。
     * @param keyword keyword参数。
     * @param category category参数。
     * @param status status参数。
     * @param pageNum pageNum参数。
     * @param pageSize pageSize参数。
     * @return 处理结果。
     */

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
    /**
     * 查询详情。
     * @param id id参数。
     * @return 处理结果。
     */

    public PromptTemplateDetail get(Long id) {
        return toDetail(selectExisting(id));
    }
    /**
     * 创建业务数据。
     * @param request request参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

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
    /**
     * 更新业务数据。
     * @param id id参数。
     * @param request request参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

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
    /**
     * 更新业务状态。
     * @param id id参数。
     * @param status status参数。
     * @param userId userId参数。
     */

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
    /**
     * 设置default字段。
     * @param id id参数。
     * @param userId userId参数。
     */

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
    /**
     * 删除业务数据。
     * @param id id参数。
     * @param userId userId参数。
     */

    @Transactional
    public void delete(Long id, Long userId) {
        var entity = selectExisting(id);
        entity.setDeleted(1);
        entity.setUpdatedBy(userId);
        promptTemplateMapper.updateById(entity);
    }
    /**
     * 执行renderForDiagnosis处理逻辑。
     * @param request request参数。
     * @return 处理结果。
     */

    public RenderedPrompt renderForDiagnosis(AnalyzeRequest request) {
        var template = selectForCategory(request.category());
        if (template == null) {
            return new RenderedPrompt(null, null, defaultPrompt(request));
        }
        return new RenderedPrompt(template.getId(), template.getVersion(), render(template.getContent(), request));
    }
    /**
     * 查询并返回符合条件的业务数据。
     * @param category category参数。
     * @return 处理结果。
     */

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
    /**
     * 执行render处理逻辑。
     * @param content content参数。
     * @param request request参数。
     * @return 处理结果。
     */

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
    /**
     * 执行defaultPrompt处理逻辑。
     * @param request request参数。
     * @return 处理结果。
     */

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
    /**
     * 查询并校验业务数据存在。
     * @param id id参数。
     * @return 处理结果。
     */

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
    /**
     * 填充实体属性。
     * @param entity entity参数。
     * @param request request参数。
     */

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
    /**
     * 执行clearDefaultForCategory处理逻辑。
     * @param category category参数。
     * @param exceptId exceptId参数。
     */

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
    /**
     * 转换为摘要视图。
     * @param entity entity参数。
     * @return 处理结果。
     */

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
    /**
     * 转换为详情视图。
     * @param entity entity参数。
     * @return 处理结果。
     */

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
    /**
     * 执行nullToDash处理逻辑。
     * @param value value参数。
     * @return 处理结果。
     */

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
    /**
     * RenderedPrompt数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record RenderedPrompt(Long templateId, String version, String content) {
    }
    /**
     * SavePromptTemplateRequest请求对象，负责承载接口入参。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record SavePromptTemplateRequest(
            String name,
            String category,
            String content,
            String version,
            Boolean defaultTemplate,
            Integer status
    ) {
    }
    /**
     * PromptTemplateSummary数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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
    /**
     * PromptTemplateDetail数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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
