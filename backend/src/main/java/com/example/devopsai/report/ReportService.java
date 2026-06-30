package com.example.devopsai.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.ErrorCode;
import com.example.devopsai.diagnosis.entity.DiagnosisMessage;
import com.example.devopsai.diagnosis.entity.DiagnosisResult;
import com.example.devopsai.diagnosis.entity.DiagnosisSession;
import com.example.devopsai.diagnosis.mapper.DiagnosisMessageMapper;
import com.example.devopsai.diagnosis.mapper.DiagnosisResultMapper;
import com.example.devopsai.diagnosis.mapper.DiagnosisSessionMapper;
import com.example.devopsai.report.entity.ReportItem;
import com.example.devopsai.report.mapper.ReportMapper;
import com.example.devopsai.report.vo.ReportDetail;
import com.example.devopsai.report.vo.ReportSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReportService {

    private static final String FORMAT_MARKDOWN = "MARKDOWN";

    private final ReportMapper reportMapper;
    private final DiagnosisSessionMapper sessionMapper;
    private final DiagnosisMessageMapper messageMapper;
    private final DiagnosisResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    public ReportService(
            ReportMapper reportMapper,
            DiagnosisSessionMapper sessionMapper,
            DiagnosisMessageMapper messageMapper,
            DiagnosisResultMapper resultMapper,
            ObjectMapper objectMapper
    ) {
        this.reportMapper = reportMapper;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.resultMapper = resultMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReportDetail createFromSession(Long sessionId, Long userId) {
        var session = selectOwnedSession(sessionId, userId);
        var messages = selectMessages(sessionId);
        var latestResult = selectLatestResult(sessionId);
        var content = buildMarkdown(session, messages, latestResult);

        var entity = new ReportItem();
        entity.setSessionId(session.getId());
        entity.setTitle(session.getTitle() + " 复盘报告");
        entity.setFormat(FORMAT_MARKDOWN);
        entity.setContent(content);
        entity.setCreatedBy(userId);
        reportMapper.insert(entity);
        return toDetail(entity);
    }

    public ReportDetail get(Long id, Long userId) {
        var entity = selectExisting(id);
        selectOwnedSession(entity.getSessionId(), userId);
        return toDetail(entity);
    }

    public List<ReportSummary> list(Long sessionId, Long userId) {
        if (sessionId != null) {
            selectOwnedSession(sessionId, userId);
        }
        var wrapper = new LambdaQueryWrapper<ReportItem>()
                .orderByDesc(ReportItem::getCreatedAt);
        if (sessionId != null) {
            wrapper.eq(ReportItem::getSessionId, sessionId);
        } else {
            wrapper.inSql(ReportItem::getSessionId,
                    "SELECT id FROM ops_diagnosis_session WHERE user_id = " + userId + " AND deleted = 0");
        }
        return reportMapper.selectList(wrapper).stream().map(this::toSummary).toList();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        var entity = selectExisting(id);
        selectOwnedSession(entity.getSessionId(), userId);
        reportMapper.deleteById(id);
    }

    private String buildMarkdown(
            DiagnosisSession session,
            List<DiagnosisMessage> messages,
            DiagnosisResult latestResult
    ) {
        var resultJson = parseJson(latestResult == null ? null : latestResult.getResultJson());
        var firstUserMessage = messages.stream()
                .filter(message -> "user".equals(message.getRole()))
                .findFirst()
                .map(DiagnosisMessage::getContent)
                .orElse("");
        var conversation = buildConversation(messages);
        var title = session.getTitle();
        return """
                # %s 复盘报告

                ## 故障标题

                %s

                ## 故障时间

                - 创建时间：%s
                - 最后更新时间：%s

                ## 影响范围

                - 运行环境：%s
                - 故障类型：%s
                - 是否生产环境：%s
                - 紧急程度：%s

                ## 故障现象

                %s

                ## 故障原因

                %s

                ## 处理过程

                %s

                ## 最终解决方案

                %s

                ## 风险点

                - 风险等级：%s
                - 是否需要重启：%s
                - 是否涉及数据风险：%s

                %s

                ## 预防措施

                %s

                ## 后续改进计划

                - 补充标准化排障步骤和命令说明。
                - 将本次处理过程沉淀到案例库或知识库。
                - 对高风险命令建立审批和备份确认机制。
                """.formatted(
                title,
                title,
                nullToDash(session.getCreatedAt()),
                nullToDash(session.getUpdatedAt()),
                nullToDash(session.getEnvironment()),
                nullToDash(session.getCategory()),
                Integer.valueOf(1).equals(session.getIsProduction()) ? "是" : "否",
                nullToDash(session.getUrgencyLevel()),
                firstText(firstUserMessage, "-"),
                firstText(
                        joinArray(resultJson, "possibleCauses"),
                        nullToDash(latestResult == null ? null : latestResult.getSummary())
                ),
                firstText(conversation, "-"),
                firstText(joinArray(resultJson, "fixSteps"), "-"),
                nullToDash(latestResult == null ? null : latestResult.getRiskLevel()),
                latestResult != null && Integer.valueOf(1).equals(latestResult.getNeedRestart()) ? "是" : "否",
                latestResult != null && Integer.valueOf(1).equals(latestResult.getDataRisk()) ? "是" : "否",
                firstText(joinArray(resultJson, "riskWarnings"), "-"),
                firstText(textField(resultJson, "prevention"), "-")
        );
    }

    private DiagnosisSession selectOwnedSession(Long sessionId, Long userId) {
        var session = sessionMapper.selectOne(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getId, sessionId)
                .eq(DiagnosisSession::getUserId, userId)
                .eq(DiagnosisSession::getDeleted, 0)
                .last("LIMIT 1"));
        if (session == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "排障会话不存在");
        }
        return session;
    }

    private ReportItem selectExisting(Long id) {
        var entity = reportMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "复盘报告不存在");
        }
        return entity;
    }

    private List<DiagnosisMessage> selectMessages(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<DiagnosisMessage>()
                .eq(DiagnosisMessage::getSessionId, sessionId)
                .orderByAsc(DiagnosisMessage::getCreatedAt));
    }

    private DiagnosisResult selectLatestResult(Long sessionId) {
        return resultMapper.selectOne(new LambdaQueryWrapper<DiagnosisResult>()
                .eq(DiagnosisResult::getSessionId, sessionId)
                .orderByDesc(DiagnosisResult::getCreatedAt)
                .last("LIMIT 1"));
    }

    private String buildConversation(List<DiagnosisMessage> messages) {
        if (messages.isEmpty()) {
            return "";
        }
        var builder = new StringBuilder();
        for (var message : messages) {
            builder.append("- ").append(message.getRole()).append("：")
                    .append(limitText(message.getContent(), 800))
                    .append("\n");
        }
        return builder.toString().trim();
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

    private String joinArray(JsonNode json, String fieldName) {
        var field = jsonField(json, fieldName);
        if (field == null || !field.isArray()) {
            return "";
        }
        var values = new java.util.ArrayList<String>();
        field.forEach(item -> values.add("- " + (item.isTextual() ? item.asText() : item.toString())));
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

    private String limitText(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String nullToDash(Object value) {
        return value == null || !StringUtils.hasText(String.valueOf(value)) ? "-" : String.valueOf(value);
    }

    private ReportSummary toSummary(ReportItem entity) {
        return new ReportSummary(
                entity.getId(),
                entity.getSessionId(),
                entity.getTitle(),
                entity.getFormat(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    private ReportDetail toDetail(ReportItem entity) {
        return new ReportDetail(
                entity.getId(),
                entity.getSessionId(),
                entity.getTitle(),
                entity.getFormat(),
                entity.getContent(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

}
