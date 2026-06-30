package com.example.devopsai.casebase;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.casebase.entity.CaseItem;
import com.example.devopsai.casebase.mapper.CaseMapper;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.diagnosis.entity.DiagnosisMessage;
import com.example.devopsai.diagnosis.entity.DiagnosisResult;
import com.example.devopsai.diagnosis.entity.DiagnosisSession;
import com.example.devopsai.diagnosis.mapper.DiagnosisMessageMapper;
import com.example.devopsai.diagnosis.mapper.DiagnosisResultMapper;
import com.example.devopsai.diagnosis.mapper.DiagnosisSessionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CaseService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final Set<String> VALID_STATUSES = Set.of("DRAFT", "PENDING_REVIEW", "PUBLISHED", "REJECTED", "OFFLINE");

    private final CaseMapper caseMapper;
    private final DiagnosisSessionMapper sessionMapper;
    private final DiagnosisMessageMapper messageMapper;
    private final DiagnosisResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    public CaseService(
            CaseMapper caseMapper,
            DiagnosisSessionMapper sessionMapper,
            DiagnosisMessageMapper messageMapper,
            DiagnosisResultMapper resultMapper,
            ObjectMapper objectMapper
    ) {
        this.caseMapper = caseMapper;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.resultMapper = resultMapper;
        this.objectMapper = objectMapper;
    }

    public PageResponse<CaseSummary> list(CaseQuery query) {
        var wrapper = new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getDeleted, 0)
                .orderByDesc(CaseItem::getUpdatedAt);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(CaseItem::getTitle, query.keyword())
                    .or()
                    .like(CaseItem::getSymptom, query.keyword())
                    .or()
                    .like(CaseItem::getCauseAnalysis, query.keyword())
                    .or()
                    .like(CaseItem::getSolution, query.keyword()));
        }
        if (StringUtils.hasText(query.category())) {
            wrapper.eq(CaseItem::getCategory, query.category());
        }
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(CaseItem::getStatus, normalizeStatus(query.status()));
        }
        if (StringUtils.hasText(query.tag())) {
            wrapper.like(CaseItem::getTags, query.tag());
        }
        var page = caseMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toSummary).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }

    public CaseDetail get(Long id) {
        return toDetail(selectExisting(id));
    }

    @Transactional
    public CaseDetail create(SaveCaseRequest request, Long userId) {
        var entity = new CaseItem();
        fill(entity, request);
        entity.setStatus(StringUtils.hasText(request.status()) ? normalizeStatus(request.status()) : STATUS_DRAFT);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        applyReviewFields(entity, entity.getStatus(), userId);
        caseMapper.insert(entity);
        return toDetail(entity);
    }

    @Transactional
    public CaseDetail update(Long id, SaveCaseRequest request, Long userId) {
        var entity = selectExisting(id);
        fill(entity, request);
        if (StringUtils.hasText(request.status())) {
            entity.setStatus(normalizeStatus(request.status()));
            applyReviewFields(entity, entity.getStatus(), userId);
        }
        entity.setUpdatedBy(userId);
        caseMapper.updateById(entity);
        return toDetail(selectExisting(id));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        var entity = selectExisting(id);
        entity.setDeleted(1);
        entity.setUpdatedBy(userId);
        caseMapper.updateById(entity);
    }

    @Transactional
    public void updateStatus(Long id, String status, Long userId) {
        var entity = selectExisting(id);
        var normalizedStatus = normalizeStatus(status);
        entity.setStatus(normalizedStatus);
        entity.setUpdatedBy(userId);
        applyReviewFields(entity, normalizedStatus, userId);
        caseMapper.updateById(entity);
    }

    @Transactional
    public CaseDetail createFromSession(Long sessionId, SaveCaseFromSessionRequest request, Long userId) {
        var session = selectOwnedSession(sessionId, userId);
        var latestResult = selectLatestResult(sessionId);
        var messages = selectSessionMessages(sessionId);
        var firstUserMessage = messages.stream()
                .filter(message -> "user".equals(message.getRole()))
                .findFirst()
                .map(DiagnosisMessage::getContent)
                .orElse("");
        var resultJson = parseJson(latestResult == null ? null : latestResult.getResultJson());
        var entity = new CaseItem();
        entity.setSourceSessionId(session.getId());
        entity.setTitle(firstText(request == null ? null : request.title(), session.getTitle()));
        entity.setCategory(firstText(request == null ? null : request.category(), session.getCategory()));
        entity.setEnvironment(firstText(request == null ? null : request.environment(), session.getEnvironment()));
        entity.setSymptom(firstText(request == null ? null : request.symptom(), firstUserMessage));
        entity.setLogContent(firstText(request == null ? null : request.logContent(), firstUserMessage));
        entity.setCauseAnalysis(firstText(request == null ? null : request.causeAnalysis(), joinArray(resultJson, "possibleCauses")));
        entity.setSolution(firstText(request == null ? null : request.solution(), buildSolution(resultJson)));
        entity.setPrevention(firstText(request == null ? null : request.prevention(), textField(resultJson, "prevention")));
        entity.setCommands(firstJson(request == null ? null : request.commands(), jsonField(resultJson, "commands")));
        entity.setTags(toJson(request == null ? null : request.tags()));
        entity.setStatus(StringUtils.hasText(request == null ? null : request.status())
                ? normalizeStatus(request.status())
                : STATUS_DRAFT);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        applyReviewFields(entity, entity.getStatus(), userId);
        caseMapper.insert(entity);
        return toDetail(entity);
    }

    private void fill(CaseItem entity, SaveCaseRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new BusinessException(400, "案例标题不能为空");
        }
        entity.setSourceSessionId(request.sourceSessionId());
        entity.setTitle(request.title());
        entity.setCategory(request.category());
        entity.setEnvironment(request.environment());
        entity.setSymptom(request.symptom());
        entity.setLogContent(request.logContent());
        entity.setCauseAnalysis(request.causeAnalysis());
        entity.setSolution(request.solution());
        entity.setPrevention(request.prevention());
        entity.setCommands(toJson(request.commands()));
        entity.setTags(toJson(request.tags()));
    }

    private CaseItem selectExisting(Long id) {
        var entity = caseMapper.selectOne(new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getId, id)
                .eq(CaseItem::getDeleted, 0)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(404, "案例不存在");
        }
        return entity;
    }

    private DiagnosisSession selectOwnedSession(Long sessionId, Long userId) {
        var session = sessionMapper.selectOne(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getId, sessionId)
                .eq(DiagnosisSession::getUserId, userId)
                .eq(DiagnosisSession::getDeleted, 0)
                .last("LIMIT 1"));
        if (session == null) {
            throw new BusinessException(404, "排障会话不存在");
        }
        return session;
    }

    private DiagnosisResult selectLatestResult(Long sessionId) {
        return resultMapper.selectOne(new LambdaQueryWrapper<DiagnosisResult>()
                .eq(DiagnosisResult::getSessionId, sessionId)
                .orderByDesc(DiagnosisResult::getCreatedAt)
                .last("LIMIT 1"));
    }

    private List<DiagnosisMessage> selectSessionMessages(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<DiagnosisMessage>()
                .eq(DiagnosisMessage::getSessionId, sessionId)
                .orderByAsc(DiagnosisMessage::getCreatedAt));
    }

    private void applyReviewFields(CaseItem entity, String status, Long userId) {
        if ("PUBLISHED".equals(status) || "REJECTED".equals(status) || "OFFLINE".equals(status)) {
            entity.setReviewedBy(userId);
            entity.setReviewedAt(LocalDateTime.now());
        }
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException(400, "案例状态不能为空");
        }
        var normalized = status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) {
            throw new BusinessException(400, "案例状态参数错误");
        }
        return normalized;
    }

    private CaseSummary toSummary(CaseItem entity) {
        return new CaseSummary(
                entity.getId(),
                entity.getSourceSessionId(),
                entity.getTitle(),
                entity.getCategory(),
                entity.getEnvironment(),
                entity.getStatus(),
                parseStringList(entity.getTags()),
                entity.getCreatedBy(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CaseDetail toDetail(CaseItem entity) {
        return new CaseDetail(
                entity.getId(),
                entity.getSourceSessionId(),
                entity.getTitle(),
                entity.getCategory(),
                entity.getEnvironment(),
                entity.getSymptom(),
                entity.getLogContent(),
                entity.getCauseAnalysis(),
                entity.getSolution(),
                entity.getPrevention(),
                parseJson(entity.getCommands()),
                parseStringList(entity.getTags()),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String buildSolution(JsonNode resultJson) {
        var fixSteps = joinArray(resultJson, "fixSteps");
        var prevention = textField(resultJson, "prevention");
        if (!StringUtils.hasText(prevention)) {
            return fixSteps;
        }
        return StringUtils.hasText(fixSteps) ? fixSteps + "\n\n预防措施：\n" + prevention : prevention;
    }

    private String joinArray(JsonNode json, String fieldName) {
        var field = jsonField(json, fieldName);
        if (field == null || !field.isArray()) {
            return "";
        }
        var values = new java.util.ArrayList<String>();
        field.forEach(item -> values.add(item.isTextual() ? item.asText() : item.toString()));
        return String.join("\n", values);
    }

    private String textField(JsonNode json, String fieldName) {
        var field = jsonField(json, fieldName);
        return field == null || field.isNull() ? "" : field.asText("");
    }

    private JsonNode jsonField(JsonNode json, String fieldName) {
        return json == null || json.isNull() ? null : json.get(fieldName);
    }

    private String firstText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String firstJson(Object value, JsonNode fallback) {
        if (value != null) {
            return toJson(value);
        }
        return fallback == null || fallback.isNull() ? null : fallback.toString();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(400, "JSON 数据格式错误");
        }
    }

    private JsonNode parseJson(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            return null;
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

    public record CaseQuery(
            String keyword,
            String category,
            String status,
            String tag,
            long pageNum,
            long pageSize
    ) {
    }

    public record SaveCaseRequest(
            Long sourceSessionId,
            @NotBlank String title,
            String category,
            String environment,
            String symptom,
            String logContent,
            String causeAnalysis,
            String solution,
            String prevention,
            Object commands,
            List<String> tags,
            String status
    ) {
    }

    public record SaveCaseFromSessionRequest(
            String title,
            String category,
            String environment,
            String symptom,
            String logContent,
            String causeAnalysis,
            String solution,
            String prevention,
            Object commands,
            List<String> tags,
            String status
    ) {
    }

    public record UpdateCaseStatusRequest(@NotBlank String status) {
    }

    public record CaseSummary(
            Long id,
            Long sourceSessionId,
            String title,
            String category,
            String environment,
            String status,
            List<String> tags,
            Long createdBy,
            Long reviewedBy,
            LocalDateTime reviewedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record CaseDetail(
            Long id,
            Long sourceSessionId,
            String title,
            String category,
            String environment,
            String symptom,
            String logContent,
            String causeAnalysis,
            String solution,
            String prevention,
            JsonNode commands,
            List<String> tags,
            String status,
            Long createdBy,
            Long reviewedBy,
            LocalDateTime reviewedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
