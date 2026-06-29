package com.example.devopsai.model;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.ai.AiProperties;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.model.entity.ModelConfig;
import com.example.devopsai.model.mapper.ModelConfigMapper;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/**
 * ModelConfigService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class ModelConfigService {

    /**
     * 模型配置数据访问对象。
     */
    private final ModelConfigMapper modelConfigMapper;
    /**
     * AI默认配置。
     */
    private final AiProperties aiProperties;
    /**
     * 创建ModelConfigService实例。
     * @param modelConfigMapper modelConfigMapper参数。
     * @param aiProperties aiProperties参数。
     */

    public ModelConfigService(ModelConfigMapper modelConfigMapper, AiProperties aiProperties) {
        this.modelConfigMapper = modelConfigMapper;
        this.aiProperties = aiProperties;
    }
    /**
     * 解析可用模型配置。
     * @param modelConfigId modelConfigId参数。
     * @return 处理结果。
     */

    public ResolvedModelConfig resolve(Long modelConfigId) {
        var modelConfig = modelConfigId == null ? selectDefaultModel() : selectById(modelConfigId);
        if (modelConfig == null) {
            return fromProperties();
        }
        var apiKey = firstText(modelConfig.getApiKeyEncrypted(), aiProperties.getApiKey());
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(400, "未配置 AI API Key，请设置 APP_AI_API_KEY 或模型配置 API Key");
        }
        return new ResolvedModelConfig(
                modelConfig.getId(),
                firstText(modelConfig.getProvider(), aiProperties.getProvider()),
                firstText(modelConfig.getApiStyle(), aiProperties.getApiStyle()),
                firstText(modelConfig.getApiBaseUrl(), aiProperties.getBaseUrl()),
                firstText(modelConfig.getModelName(), aiProperties.getModel()),
                apiKey,
                firstInt(modelConfig.getMaxTokens(), aiProperties.getMaxTokens()),
                firstDouble(modelConfig.getTemperature(), aiProperties.getTemperature()),
                firstInt(modelConfig.getTimeoutSeconds(), aiProperties.getTimeoutSeconds())
        );
    }
    /**
     * 执行list处理逻辑。
     * @param keyword keyword参数。
     * @param status status参数。
     * @param pageNum pageNum参数。
     * @param pageSize pageSize参数。
     * @return 处理结果。
     */

    public PageResponse<ModelConfigSummary> list(String keyword, Integer status, long pageNum, long pageSize) {
        var wrapper = new LambdaQueryWrapper<ModelConfig>()
                .eq(ModelConfig::getDeleted, 0)
                .orderByDesc(ModelConfig::getDefaultModel)
                .orderByDesc(ModelConfig::getUpdatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ModelConfig::getProvider, keyword)
                    .or()
                    .like(ModelConfig::getModelName, keyword)
                    .or()
                    .like(ModelConfig::getApiBaseUrl, keyword));
        }
        if (status != null) {
            wrapper.eq(ModelConfig::getStatus, status);
        }
        IPage<ModelConfig> page = modelConfigMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }
    /**
     * 查询详情。
     * @param id id参数。
     * @return 处理结果。
     */

    public ModelConfigDetail get(Long id) {
        return toDetail(selectExisting(id));
    }
    /**
     * 创建业务数据。
     * @param request request参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

    @Transactional
    public ModelConfigDetail create(SaveModelConfigRequest request, Long userId) {
        var entity = new ModelConfig();
        fill(entity, request);
        entity.setStatus(request.status() == null ? 1 : request.status());
        entity.setDefaultModel(Boolean.TRUE.equals(request.defaultModel()) ? 1 : 0);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        if (Integer.valueOf(1).equals(entity.getDefaultModel())) {
            clearDefaultModel();
        }
        modelConfigMapper.insert(entity);
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
    public ModelConfigDetail update(Long id, SaveModelConfigRequest request, Long userId) {
        var entity = selectExisting(id);
        fill(entity, request);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.defaultModel() != null) {
            entity.setDefaultModel(request.defaultModel() ? 1 : 0);
        }
        entity.setUpdatedBy(userId);
        if (Integer.valueOf(1).equals(entity.getDefaultModel())) {
            clearDefaultModelExcept(id);
        }
        modelConfigMapper.updateById(entity);
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
        modelConfigMapper.updateById(entity);
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
            throw new BusinessException(400, "停用的模型配置不能设为默认");
        }
        clearDefaultModelExcept(id);
        entity.setDefaultModel(1);
        entity.setUpdatedBy(userId);
        modelConfigMapper.updateById(entity);
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
        modelConfigMapper.updateById(entity);
    }
    /**
     * 解析用于测试的模型配置。
     * @param id id参数。
     * @param apiKeyOverride apiKeyOverride参数。
     * @return 处理结果。
     */

    public ResolvedModelConfig resolveForTest(Long id, String apiKeyOverride) {
        var entity = selectExisting(id);
        var apiKey = firstText(apiKeyOverride, firstText(entity.getApiKeyEncrypted(), aiProperties.getApiKey()));
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(400, "未配置 AI API Key");
        }
        return new ResolvedModelConfig(
                entity.getId(),
                firstText(entity.getProvider(), aiProperties.getProvider()),
                firstText(entity.getApiStyle(), aiProperties.getApiStyle()),
                firstText(entity.getApiBaseUrl(), aiProperties.getBaseUrl()),
                firstText(entity.getModelName(), aiProperties.getModel()),
                apiKey,
                firstInt(entity.getMaxTokens(), aiProperties.getMaxTokens()),
                firstDouble(entity.getTemperature(), aiProperties.getTemperature()),
                firstInt(entity.getTimeoutSeconds(), aiProperties.getTimeoutSeconds())
        );
    }
    /**
     * 按ID查询数据。
     * @param modelConfigId modelConfigId参数。
     * @return 处理结果。
     */

    private ModelConfig selectById(Long modelConfigId) {
        var modelConfig = modelConfigMapper.selectOne(new LambdaQueryWrapper<ModelConfig>()
                .eq(ModelConfig::getId, modelConfigId)
                .eq(ModelConfig::getStatus, 1)
                .eq(ModelConfig::getDeleted, 0)
                .last("LIMIT 1"));
        if (modelConfig == null) {
            throw new BusinessException(404, "模型配置不存在或已停用");
        }
        return modelConfig;
    }
    /**
     * 查询默认模型配置。
     * @return 处理结果。
     */

    private ModelConfig selectDefaultModel() {
        return modelConfigMapper.selectOne(new LambdaQueryWrapper<ModelConfig>()
                .eq(ModelConfig::getDefaultModel, 1)
                .eq(ModelConfig::getStatus, 1)
                .eq(ModelConfig::getDeleted, 0)
                .orderByDesc(ModelConfig::getUpdatedAt)
                .last("LIMIT 1"));
    }
    /**
     * 查询并校验业务数据存在。
     * @param id id参数。
     * @return 处理结果。
     */

    private ModelConfig selectExisting(Long id) {
        var entity = modelConfigMapper.selectOne(new LambdaQueryWrapper<ModelConfig>()
                .eq(ModelConfig::getId, id)
                .eq(ModelConfig::getDeleted, 0)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(404, "模型配置不存在");
        }
        return entity;
    }
    /**
     * 填充实体属性。
     * @param entity entity参数。
     * @param request request参数。
     */

    private void fill(ModelConfig entity, SaveModelConfigRequest request) {
        if (!StringUtils.hasText(request.provider())) {
            throw new BusinessException(400, "模型供应商不能为空");
        }
        if (!StringUtils.hasText(request.modelName())) {
            throw new BusinessException(400, "模型名称不能为空");
        }
        if (!StringUtils.hasText(request.apiBaseUrl())) {
            throw new BusinessException(400, "API Base URL 不能为空");
        }
        entity.setProvider(request.provider());
        entity.setApiStyle(StringUtils.hasText(request.apiStyle()) ? request.apiStyle() : "RESPONSES");
        entity.setModelName(request.modelName());
        entity.setApiBaseUrl(request.apiBaseUrl());
        if (StringUtils.hasText(request.apiKey())) {
            entity.setApiKeyEncrypted(request.apiKey());
        }
        entity.setMaxTokens(request.maxTokens() == null ? 4096 : request.maxTokens());
        entity.setTemperature(BigDecimal.valueOf(request.temperature() == null ? 0.3 : request.temperature()));
        entity.setTimeoutSeconds(request.timeoutSeconds() == null ? 60 : request.timeoutSeconds());
    }
    /**
     * 执行clearDefaultModel处理逻辑。
     */

    private void clearDefaultModel() {
        modelConfigMapper.selectList(new LambdaQueryWrapper<ModelConfig>()
                        .eq(ModelConfig::getDefaultModel, 1)
                        .eq(ModelConfig::getDeleted, 0))
                .forEach(item -> {
                    item.setDefaultModel(0);
                    modelConfigMapper.updateById(item);
                });
    }
    /**
     * 执行clearDefaultModelExcept处理逻辑。
     * @param id id参数。
     */

    private void clearDefaultModelExcept(Long id) {
        modelConfigMapper.selectList(new LambdaQueryWrapper<ModelConfig>()
                        .eq(ModelConfig::getDefaultModel, 1)
                        .eq(ModelConfig::getDeleted, 0)
                        .ne(ModelConfig::getId, id))
                .forEach(item -> {
                    item.setDefaultModel(0);
                    modelConfigMapper.updateById(item);
                });
    }
    /**
     * 转换为摘要视图。
     * @param entity entity参数。
     * @return 处理结果。
     */

    private ModelConfigSummary toSummary(ModelConfig entity) {
        return new ModelConfigSummary(
                entity.getId(),
                entity.getProvider(),
                entity.getApiStyle(),
                entity.getModelName(),
                entity.getApiBaseUrl(),
                entity.getMaxTokens(),
                firstDouble(entity.getTemperature(), null),
                entity.getTimeoutSeconds(),
                Integer.valueOf(1).equals(entity.getDefaultModel()),
                Integer.valueOf(1).equals(entity.getStatus()),
                StringUtils.hasText(entity.getApiKeyEncrypted()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    /**
     * 转换为详情视图。
     * @param entity entity参数。
     * @return 处理结果。
     */

    private ModelConfigDetail toDetail(ModelConfig entity) {
        var summary = toSummary(entity);
        return new ModelConfigDetail(
                summary.id(),
                summary.provider(),
                summary.apiStyle(),
                summary.modelName(),
                summary.apiBaseUrl(),
                summary.maxTokens(),
                summary.temperature(),
                summary.timeoutSeconds(),
                summary.defaultModel(),
                summary.enabled(),
                summary.hasApiKey(),
                summary.createdAt(),
                summary.updatedAt()
        );
    }
    /**
     * 执行fromProperties处理逻辑。
     * @return 处理结果。
     */

    private ResolvedModelConfig fromProperties() {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new BusinessException(400, "未配置 AI API Key，请设置 APP_AI_API_KEY");
        }
        return new ResolvedModelConfig(
                null,
                aiProperties.getProvider(),
                aiProperties.getApiStyle(),
                aiProperties.getBaseUrl(),
                aiProperties.getModel(),
                aiProperties.getApiKey(),
                aiProperties.getMaxTokens(),
                aiProperties.getTemperature(),
                aiProperties.getTimeoutSeconds()
        );
    }
    /**
     * 执行firstText处理逻辑。
     * @param value value参数。
     * @param fallback fallback参数。
     * @return 处理结果。
     */

    private String firstText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
    /**
     * 执行firstInt处理逻辑。
     * @param value value参数。
     * @param fallback fallback参数。
     * @return 处理结果。
     */

    private Integer firstInt(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }
    /**
     * 执行firstDouble处理逻辑。
     * @param value value参数。
     * @param fallback fallback参数。
     * @return 处理结果。
     */

    private Double firstDouble(BigDecimal value, Double fallback) {
        return value == null ? fallback : value.doubleValue();
    }
    /**
     * ResolvedModelConfig配置类，负责声明对应模块的基础配置。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record ResolvedModelConfig(
            Long id,
            String provider,
            String apiStyle,
            String baseUrl,
            String model,
            String apiKey,
            Integer maxTokens,
            Double temperature,
            Integer timeoutSeconds
    ) {
    }
    /**
     * SaveModelConfigRequest请求对象，负责承载接口入参。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record SaveModelConfigRequest(
            String provider,
            String apiStyle,
            String modelName,
            String apiBaseUrl,
            String apiKey,
            Integer maxTokens,
            Double temperature,
            Integer timeoutSeconds,
            Boolean defaultModel,
            Integer status
    ) {
    }
    /**
     * ModelConfigSummary数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record ModelConfigSummary(
            Long id,
            String provider,
            String apiStyle,
            String modelName,
            String apiBaseUrl,
            Integer maxTokens,
            Double temperature,
            Integer timeoutSeconds,
            Boolean defaultModel,
            Boolean enabled,
            Boolean hasApiKey,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
    /**
     * ModelConfigDetail数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record ModelConfigDetail(
            Long id,
            String provider,
            String apiStyle,
            String modelName,
            String apiBaseUrl,
            Integer maxTokens,
            Double temperature,
            Integer timeoutSeconds,
            Boolean defaultModel,
            Boolean enabled,
            Boolean hasApiKey,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
}
