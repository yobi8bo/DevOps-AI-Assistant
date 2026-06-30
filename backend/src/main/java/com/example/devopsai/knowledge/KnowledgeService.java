package com.example.devopsai.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.casebase.entity.CaseItem;
import com.example.devopsai.casebase.mapper.CaseMapper;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.ErrorCode;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.diagnosis.dto.AnalyzeRequest;
import com.example.devopsai.knowledge.dto.KnowledgeQuery;
import com.example.devopsai.knowledge.dto.SaveKnowledgeFromCaseRequest;
import com.example.devopsai.knowledge.dto.SaveKnowledgeRequest;
import com.example.devopsai.knowledge.entity.KnowledgeItem;
import com.example.devopsai.knowledge.mapper.KnowledgeMapper;
import com.example.devopsai.knowledge.vo.KnowledgeDetail;
import com.example.devopsai.knowledge.vo.KnowledgeSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class KnowledgeService {

    private static final String CONTENT_TYPE_MARKDOWN = "MARKDOWN";
    private static final String SOURCE_TYPE_MANUAL = "MANUAL";
    private static final String SOURCE_TYPE_CASE = "CASE";
    private static final String DEFAULT_VERSION = "1.0.0";

    private final KnowledgeMapper knowledgeMapper;
    private final CaseMapper caseMapper;
    private final ObjectMapper objectMapper;

    public KnowledgeService(KnowledgeMapper knowledgeMapper, CaseMapper caseMapper, ObjectMapper objectMapper) {
        this.knowledgeMapper = knowledgeMapper;
        this.caseMapper = caseMapper;
        this.objectMapper = objectMapper;
    }

    public PageResponse<KnowledgeSummary> list(KnowledgeQuery query) {
        var wrapper = new LambdaQueryWrapper<KnowledgeItem>()
                .eq(KnowledgeItem::getDeleted, 0)
                .orderByDesc(KnowledgeItem::getUpdatedAt);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(KnowledgeItem::getTitle, query.keyword())
                    .or()
                    .like(KnowledgeItem::getContent, query.keyword())
                    .or()
                    .like(KnowledgeItem::getSourceRef, query.keyword()));
        }
        if (StringUtils.hasText(query.category())) {
            wrapper.eq(KnowledgeItem::getCategory, query.category());
        }
        if (StringUtils.hasText(query.tag())) {
            wrapper.like(KnowledgeItem::getTags, query.tag());
        }
        if (query.status() != null) {
            validateStatus(query.status());
            wrapper.eq(KnowledgeItem::getStatus, query.status());
        }
        var page = knowledgeMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public KnowledgeDetail get(Long id) {
        return toDetail(selectExisting(id));
    }

    public String buildRelevantKnowledgeContext(AnalyzeRequest request) {
        var keywords = extractKeywords(request);
        if (!StringUtils.hasText(request.category()) && keywords.isEmpty()) {
            return "";
        }

        var wrapper = new LambdaQueryWrapper<KnowledgeItem>()
                .eq(KnowledgeItem::getDeleted, 0)
                .eq(KnowledgeItem::getStatus, 1)
                .orderByDesc(KnowledgeItem::getUpdatedAt)
                .last("LIMIT 5");
        wrapper.and(w -> {
            var hasCondition = false;
            if (StringUtils.hasText(request.category())) {
                w.eq(KnowledgeItem::getCategory, request.category());
                hasCondition = true;
            }
            for (var keyword : keywords) {
                if (hasCondition) {
                    w.or();
                }
                w.like(KnowledgeItem::getTitle, keyword)
                        .or()
                        .like(KnowledgeItem::getContent, keyword)
                        .or()
                        .like(KnowledgeItem::getTags, keyword);
                hasCondition = true;
            }
        });

        var items = knowledgeMapper.selectList(wrapper);
        if (items.isEmpty()) {
            return "";
        }

        var builder = new StringBuilder();
        for (var item : items) {
            var snippet = limitText(item.getContent(), 900);
            if (!StringUtils.hasText(snippet)) {
                continue;
            }
            var block = """
                    - 标题：%s
                      分类：%s
                      内容：
                    %s

                    """.formatted(item.getTitle(), nullToDash(item.getCategory()), snippet);
            if (builder.length() + block.length() > 4000) {
                break;
            }
            builder.append(block);
        }
        return builder.toString().trim();
    }

    @Transactional
    public KnowledgeDetail create(SaveKnowledgeRequest request, Long userId) {
        var entity = new KnowledgeItem();
        fill(entity, request);
        entity.setStatus(request.status() == null ? 1 : request.status());
        validateStatus(entity.getStatus());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        knowledgeMapper.insert(entity);
        return toDetail(entity);
    }

    @Transactional
    public KnowledgeDetail update(Long id, SaveKnowledgeRequest request, Long userId) {
        var entity = selectExisting(id);
        fill(entity, request);
        if (request.status() != null) {
            validateStatus(request.status());
            entity.setStatus(request.status());
        }
        entity.setUpdatedBy(userId);
        knowledgeMapper.updateById(entity);
        return toDetail(selectExisting(id));
    }

    @Transactional
    public void updateStatus(Long id, Integer status, Long userId) {
        validateStatus(status);
        var entity = selectExisting(id);
        entity.setStatus(status);
        entity.setUpdatedBy(userId);
        knowledgeMapper.updateById(entity);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        var entity = selectExisting(id);
        entity.setDeleted(1);
        entity.setUpdatedBy(userId);
        knowledgeMapper.updateById(entity);
    }

    @Transactional
    public KnowledgeDetail createFromCase(Long caseId, SaveKnowledgeFromCaseRequest request, Long userId) {
        var caseItem = caseMapper.selectOne(new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getId, caseId)
                .eq(CaseItem::getDeleted, 0)
                .last("LIMIT 1"));
        if (caseItem == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "案例不存在");
        }
        if (!"PUBLISHED".equals(caseItem.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "只有已发布案例才能转入知识库");
        }

        var entity = new KnowledgeItem();
        entity.setTitle(firstText(request == null ? null : request.title(), caseItem.getTitle()));
        entity.setCategory(firstText(request == null ? null : request.category(), caseItem.getCategory()));
        entity.setTags(firstJson(request == null ? null : request.tags(), caseItem.getTags()));
        entity.setContent(firstText(request == null ? null : request.content(), buildMarkdownFromCase(caseItem)));
        entity.setContentType(CONTENT_TYPE_MARKDOWN);
        entity.setSourceType(SOURCE_TYPE_CASE);
        entity.setSourceRef("case:" + caseItem.getId());
        entity.setVersion(firstText(request == null ? null : request.version(), DEFAULT_VERSION));
        entity.setStatus(request == null || request.status() == null ? 1 : request.status());
        validateStatus(entity.getStatus());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        knowledgeMapper.insert(entity);
        return toDetail(entity);
    }

    private void fill(KnowledgeItem entity, SaveKnowledgeRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "知识标题不能为空");
        }
        if (!StringUtils.hasText(request.content())) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "知识正文不能为空");
        }
        entity.setTitle(request.title());
        entity.setCategory(request.category());
        entity.setTags(toJson(request.tags()));
        entity.setContent(request.content());
        entity.setContentType(StringUtils.hasText(request.contentType())
                ? request.contentType()
                : CONTENT_TYPE_MARKDOWN);
        entity.setSourceType(StringUtils.hasText(request.sourceType())
                ? request.sourceType()
                : SOURCE_TYPE_MANUAL);
        entity.setSourceRef(request.sourceRef());
        entity.setVersion(StringUtils.hasText(request.version()) ? request.version() : DEFAULT_VERSION);
    }

    private KnowledgeItem selectExisting(Long id) {
        var entity = knowledgeMapper.selectOne(new LambdaQueryWrapper<KnowledgeItem>()
                .eq(KnowledgeItem::getId, id)
                .eq(KnowledgeItem::getDeleted, 0)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "知识不存在");
        }
        return entity;
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "知识状态参数错误");
        }
    }

    private KnowledgeSummary toSummary(KnowledgeItem entity) {
        return new KnowledgeSummary(
                entity.getId(),
                entity.getTitle(),
                entity.getCategory(),
                parseStringList(entity.getTags()),
                entity.getContentType(),
                entity.getSourceType(),
                entity.getSourceRef(),
                entity.getVersion(),
                Integer.valueOf(1).equals(entity.getStatus()),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private KnowledgeDetail toDetail(KnowledgeItem entity) {
        return new KnowledgeDetail(
                entity.getId(),
                entity.getTitle(),
                entity.getCategory(),
                parseStringList(entity.getTags()),
                entity.getContent(),
                entity.getContentType(),
                entity.getSourceType(),
                entity.getSourceRef(),
                entity.getVersion(),
                Integer.valueOf(1).equals(entity.getStatus()),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String buildMarkdownFromCase(CaseItem caseItem) {
        var builder = new StringBuilder();
        appendSection(builder, "故障现象", caseItem.getSymptom());
        appendSection(builder, "运行环境", caseItem.getEnvironment());
        appendSection(builder, "原始日志", caseItem.getLogContent());
        appendSection(builder, "原因分析", caseItem.getCauseAnalysis());
        appendSection(builder, "处理步骤", caseItem.getSolution());
        appendSection(builder, "预防措施", caseItem.getPrevention());
        appendSection(builder, "关联命令", caseItem.getCommands());
        return builder.toString().trim();
    }

    private void appendSection(StringBuilder builder, String title, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        builder.append("## ").append(title).append("\n\n")
                .append(content).append("\n\n");
    }

    private String firstText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private List<String> extractKeywords(AnalyzeRequest request) {
        var source = String.join(" ",
                nullToEmpty(request.title()),
                nullToEmpty(request.category()),
                nullToEmpty(request.environment()),
                nullToEmpty(request.osInfo()),
                nullToEmpty(request.middleware()),
                nullToEmpty(request.serviceType()),
                nullToEmpty(request.description()),
                nullToEmpty(request.logContent()),
                nullToEmpty(request.commandOutput())
        );
        if (!StringUtils.hasText(source)) {
            return List.of();
        }
        var keywords = new LinkedHashSet<String>();
        for (var token : source.split("[\\s,，。；;:：/\\\\|\\[\\](){}<>\"'`]+")) {
            var value = token.trim();
            if (value.length() >= 2 && value.length() <= 40 && !isNoiseKeyword(value)) {
                keywords.add(value);
            }
            if (keywords.size() >= 8) {
                break;
            }
        }
        return List.copyOf(keywords);
    }

    private boolean isNoiseKeyword(String value) {
        return "-".equals(value)
                || "true".equalsIgnoreCase(value)
                || "false".equalsIgnoreCase(value)
                || "null".equalsIgnoreCase(value);
    }

    private String limitText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        var trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "\n...";
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String nullToEmpty(String value) {
        return StringUtils.hasText(value) ? value : "";
    }

    private String firstJson(List<String> value, String fallback) {
        if (value != null) {
            return toJson(value);
        }
        return StringUtils.hasText(fallback) ? fallback : null;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.COMMON_PARAM_INVALID, "JSON 数据格式错误");
        }
    }

    private List<String> parseStringList(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        try {
            var type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

}
