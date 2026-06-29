package com.example.devopsai.diagnosis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.ai.AiDiagnosisService;
import com.example.devopsai.common.BusinessException;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeRequest;
import com.example.devopsai.diagnosis.DiagnosisController.AnalyzeResponse;
import com.example.devopsai.diagnosis.DiagnosisController.CommandSuggestion;
import com.example.devopsai.diagnosis.DiagnosisController.FollowUpRequest;
import com.example.devopsai.diagnosis.DiagnosisController.MessageItem;
import com.example.devopsai.diagnosis.DiagnosisController.ResultItem;
import com.example.devopsai.diagnosis.DiagnosisController.SessionDetail;
import com.example.devopsai.diagnosis.DiagnosisController.SessionQuery;
import com.example.devopsai.diagnosis.DiagnosisController.SessionSummary;
import com.example.devopsai.diagnosis.entity.DiagnosisMessage;
import com.example.devopsai.diagnosis.entity.DiagnosisResult;
import com.example.devopsai.diagnosis.entity.DiagnosisSession;
import com.example.devopsai.diagnosis.mapper.DiagnosisMessageMapper;
import com.example.devopsai.diagnosis.mapper.DiagnosisResultMapper;
import com.example.devopsai.diagnosis.mapper.DiagnosisSessionMapper;
import com.example.devopsai.risk.RiskDetectionService;
import com.example.devopsai.risk.RiskLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/**
 * DiagnosisService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class DiagnosisService {

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(DiagnosisService.class);

    /**
     * 默认诊断状态。
     */
    private static final String DEFAULT_STATUS = "WAITING_CONFIRM";

    /**
     * 诊断会话数据访问对象。
     */
    private final DiagnosisSessionMapper sessionMapper;
    /**
     * 诊断消息数据访问对象。
     */
    private final DiagnosisMessageMapper messageMapper;
    /**
     * 诊断结果数据访问对象。
     */
    private final DiagnosisResultMapper resultMapper;
    /**
     * AI诊断服务。
     */
    private final AiDiagnosisService aiDiagnosisService;
    /**
     * 风险检测服务。
     */
    private final RiskDetectionService riskDetectionService;
    /**
     * JSON序列化组件。
     */
    private final ObjectMapper objectMapper;
    /**
     * 创建DiagnosisService实例。
     * @param sessionMapper sessionMapper参数。
     * @param messageMapper messageMapper参数。
     * @param resultMapper resultMapper参数。
     * @param aiDiagnosisService aiDiagnosisService参数。
     * @param riskDetectionService riskDetectionService参数。
     * @param objectMapper objectMapper参数。
     */

    public DiagnosisService(
            DiagnosisSessionMapper sessionMapper,
            DiagnosisMessageMapper messageMapper,
            DiagnosisResultMapper resultMapper,
            AiDiagnosisService aiDiagnosisService,
            RiskDetectionService riskDetectionService,
            ObjectMapper objectMapper
    ) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.resultMapper = resultMapper;
        this.aiDiagnosisService = aiDiagnosisService;
        this.riskDetectionService = riskDetectionService;
        this.objectMapper = objectMapper;
    }
    /**
     * 执行智能诊断分析。
     * @param request request参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

    @Transactional
    public AnalyzeResponse analyze(AnalyzeRequest request, Long userId) {
        var now = LocalDateTime.now();
        var session = new DiagnosisSession();
        session.setTitle(request.title());
        session.setCategory(request.category());
        session.setEnvironment(request.environment());
        session.setOsInfo(request.osInfo());
        session.setMiddleware(request.middleware());
        session.setServiceType(request.serviceType());
        session.setIsProduction(Boolean.TRUE.equals(request.isProduction()) ? 1 : 0);
        session.setUrgencyLevel(request.urgencyLevel());
        session.setStatus(DEFAULT_STATUS);
        session.setUserId(userId);
        session.setLastMessageAt(now);
        session.setCreatedBy(userId);
        session.setUpdatedBy(userId);
        sessionMapper.insert(session);
        log.info("diagnosis_session_created sessionId={} userId={} category={} production={}",
                session.getId(), userId, request.category(), Boolean.TRUE.equals(request.isProduction()));

        var userMessage = new DiagnosisMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(buildUserContent(request));
        userMessage.setContentSanitized(userMessage.getContent());
        userMessage.setCreatedBy(userId);
        messageMapper.insert(userMessage);

        return saveAiAnalysis(session, request, userId);
    }

    /**
     * 在已有会话中追加追问并重新生成诊断结果。
     * @param id 会话ID。
     * @param request 追问请求。
     * @param userId 用户ID。
     * @return 最新分析结果。
     */
    @Transactional
    public AnalyzeResponse continueAnalyze(Long id, FollowUpRequest request, Long userId) {
        var session = selectOwnedSession(id, userId);
        var context = buildConversationContext(id);

        var userMessage = new DiagnosisMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.content());
        userMessage.setContentSanitized(request.content());
        userMessage.setCreatedBy(userId);
        messageMapper.insert(userMessage);

        var followUpAnalyzeRequest = new AnalyzeRequest(
                session.getTitle(),
                session.getCategory(),
                session.getEnvironment(),
                session.getOsInfo(),
                session.getMiddleware(),
                session.getServiceType(),
                Integer.valueOf(1).equals(session.getIsProduction()),
                session.getUrgencyLevel(),
                buildFollowUpDescription(context, request.content()),
                null,
                request.content(),
                request.modelConfigId()
        );
        session.setStatus(DEFAULT_STATUS);
        session.setLastMessageAt(LocalDateTime.now());
        session.setUpdatedBy(userId);
        sessionMapper.updateById(session);

        log.info("diagnosis_follow_up_created sessionId={} userId={} messageId={}",
                session.getId(), userId, userMessage.getId());
        return saveAiAnalysis(session, followUpAnalyzeRequest, userId);
    }

    /**
     * 调用AI并保存助手消息和结构化结果。
     * @param session 诊断会话。
     * @param request 分析请求。
     * @param userId 用户ID。
     * @return 最新分析结果。
     */
    private AnalyzeResponse saveAiAnalysis(
            DiagnosisSession session,
            AnalyzeRequest request,
            Long userId
    ) {
        var aiResult = aiDiagnosisService.analyze(request, session.getId(), userId);
        var response = applyRiskDetection(aiResult.response(), request);

        var assistantMessage = new DiagnosisMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(response.summary());
        assistantMessage.setContentSanitized(response.summary());
        assistantMessage.setCreatedBy(userId);
        messageMapper.insert(assistantMessage);

        response = new AnalyzeResponse(
                response.sessionId(),
                assistantMessage.getId(),
                null,
                response.summary(),
                response.possibleCauses(),
                response.checkSteps(),
                response.fixSteps(),
                response.commands(),
                response.riskLevel(),
                response.riskWarnings(),
                response.needRestart(),
                response.dataRisk(),
                response.prevention(),
                response.needMoreInfo()
        );

        var result = new DiagnosisResult();
        result.setSessionId(session.getId());
        result.setMessageId(assistantMessage.getId());
        result.setSummary(response.summary());
        result.setResultJson(toJson(response));
        result.setRawResponse(aiResult.rawResponse());
        result.setRiskLevel(response.riskLevel());
        result.setNeedRestart(response.needRestart() ? 1 : 0);
        result.setDataRisk(response.dataRisk() ? 1 : 0);
        result.setModelConfigId(aiResult.modelConfigId());
        result.setPromptTemplateId(aiResult.promptTemplateId());
        result.setPromptVersion(aiResult.promptVersion());
        result.setCreatedBy(userId);
        resultMapper.insert(result);
        log.info("diagnosis_analysis_saved sessionId={} resultId={} userId={} riskLevel={} modelConfigId={} promptTemplateId={} promptVersion={}",
                session.getId(), result.getId(), userId, response.riskLevel(),
                aiResult.modelConfigId(), aiResult.promptTemplateId(), aiResult.promptVersion());

        return new AnalyzeResponse(
                response.sessionId(),
                response.messageId(),
                result.getId(),
                response.summary(),
                response.possibleCauses(),
                response.checkSteps(),
                response.fixSteps(),
                response.commands(),
                response.riskLevel(),
                response.riskWarnings(),
                response.needRestart(),
                response.dataRisk(),
                response.prevention(),
                response.needMoreInfo()
        );
    }
    /**
     * 执行listSessions处理逻辑。
     * @param query query参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

    public PageResponse<SessionSummary> listSessions(SessionQuery query, Long userId) {
        var wrapper = new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getUserId, userId)
                .eq(DiagnosisSession::getDeleted, 0)
                .orderByDesc(DiagnosisSession::getUpdatedAt);

        if (StringUtils.hasText(query.keyword())) {
            wrapper.like(DiagnosisSession::getTitle, query.keyword());
        }
        if (StringUtils.hasText(query.category())) {
            wrapper.eq(DiagnosisSession::getCategory, query.category());
        }
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(DiagnosisSession::getStatus, query.status());
        }
        if (query.isProduction() != null) {
            wrapper.eq(DiagnosisSession::getIsProduction, query.isProduction() ? 1 : 0);
        }

        IPage<DiagnosisSession> page = sessionMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream()
                .map(session -> {
                    var latest = selectLatestResult(session.getId());
                    return new SessionSummary(
                            session.getId(),
                            session.getTitle(),
                            session.getCategory(),
                            session.getEnvironment(),
                            Integer.valueOf(1).equals(session.getIsProduction()),
                            session.getUrgencyLevel(),
                            session.getStatus(),
                            latest == null ? "LOW" : latest.getRiskLevel(),
                            latest == null ? "" : latest.getSummary(),
                            session.getCreatedAt(),
                            session.getUpdatedAt()
                    );
                })
                .toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }
    /**
     * 获取诊断会话。
     * @param id id参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

    public SessionDetail getSession(Long id, Long userId) {
        var session = selectOwnedSession(id, userId);
        var messages = messageMapper.selectList(new LambdaQueryWrapper<DiagnosisMessage>()
                        .eq(DiagnosisMessage::getSessionId, id)
                        .orderByAsc(DiagnosisMessage::getCreatedAt))
                .stream()
                .map(message -> new MessageItem(message.getId(), message.getRole(), message.getContent(), message.getCreatedAt()))
                .toList();
        var latest = selectLatestResult(id);
        var result = latest == null ? null : new ResultItem(
                latest.getId(),
                latest.getSummary(),
                latest.getRiskLevel(),
                latest.getResultJson(),
                latest.getModelConfigId(),
                latest.getPromptTemplateId(),
                latest.getPromptVersion(),
                latest.getCreatedAt()
        );
        return new SessionDetail(
                session.getId(),
                session.getTitle(),
                session.getCategory(),
                session.getEnvironment(),
                Integer.valueOf(1).equals(session.getIsProduction()),
                session.getUrgencyLevel(),
                session.getStatus(),
                messages,
                result,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
    /**
     * 更新业务状态。
     * @param id id参数。
     * @param status status参数。
     * @param userId userId参数。
     */

    @Transactional
    public void updateStatus(Long id, String status, Long userId) {
        var session = selectOwnedSession(id, userId);
        session.setStatus(status);
        session.setUpdatedBy(userId);
        sessionMapper.updateById(session);
        log.info("diagnosis_status_updated sessionId={} userId={} status={}", id, userId, status);
    }
    /**
     * 删除诊断会话。
     * @param id id参数。
     * @param userId userId参数。
     */

    @Transactional
    public void deleteSession(Long id, Long userId) {
        int updated = sessionMapper.update(null, new LambdaUpdateWrapper<DiagnosisSession>()
                .set(DiagnosisSession::getDeleted, 1)
                .set(DiagnosisSession::getUpdatedBy, userId)
                .eq(DiagnosisSession::getId, id)
                .eq(DiagnosisSession::getUserId, userId)
                .eq(DiagnosisSession::getDeleted, 0));
        if (updated == 0) {
            throw new BusinessException(404, "排障会话不存在");
        }
        log.info("diagnosis_session_deleted sessionId={} userId={}", id, userId);
    }
    /**
     * 查询当前用户拥有的诊断会话。
     * @param id id参数。
     * @param userId userId参数。
     * @return 处理结果。
     */

    private DiagnosisSession selectOwnedSession(Long id, Long userId) {
        var session = sessionMapper.selectOne(new LambdaQueryWrapper<DiagnosisSession>()
                .eq(DiagnosisSession::getId, id)
                .eq(DiagnosisSession::getUserId, userId)
                .eq(DiagnosisSession::getDeleted, 0)
                .last("LIMIT 1"));
        if (session == null) {
            throw new BusinessException(404, "排障会话不存在");
        }
        return session;
    }
    /**
     * 查询会话最新诊断结果。
     * @param sessionId sessionId参数。
     * @return 处理结果。
     */

    private DiagnosisResult selectLatestResult(Long sessionId) {
        return resultMapper.selectOne(new LambdaQueryWrapper<DiagnosisResult>()
                .eq(DiagnosisResult::getSessionId, sessionId)
                .orderByDesc(DiagnosisResult::getCreatedAt)
                .last("LIMIT 1"));
    }

    /**
     * 构建用于多轮追问的会话上下文。
     * @param sessionId 会话ID。
     * @return 会话上下文。
     */
    private String buildConversationContext(Long sessionId) {
        var messages = messageMapper.selectList(new LambdaQueryWrapper<DiagnosisMessage>()
                .eq(DiagnosisMessage::getSessionId, sessionId)
                .orderByAsc(DiagnosisMessage::getCreatedAt));
        if (messages.isEmpty()) {
            return "";
        }
        var builder = new StringBuilder();
        for (var message : messages) {
            builder.append(message.getRole()).append("：\n")
                    .append(message.getContent()).append("\n\n");
        }
        var latest = selectLatestResult(sessionId);
        if (latest != null) {
            builder.append("latest_result：\n")
                    .append(blankToDash(latest.getResultJson()))
                    .append("\n\n");
        }
        return builder.toString().trim();
    }

    /**
     * 构建继续追问的完整诊断描述。
     * @param context 历史会话上下文。
     * @param content 本次追问内容。
     * @return 完整诊断描述。
     */
    private String buildFollowUpDescription(String context, String content) {
        return """
                以下是同一次故障排查会话的历史上下文，请基于上下文和本次补充继续分析，必要时修正之前的判断。

                历史上下文：
                %s

                本次补充：
                %s
                """.formatted(blankToDash(context), content);
    }
    /**
     * 构建兜底诊断结果。
     * @param sessionId sessionId参数。
     * @param messageId messageId参数。
     * @return 处理结果。
     */

    private AnalyzeResponse buildStubAnalysis(Long sessionId, Long messageId) {
        var commands = List.of(
                new CommandSuggestion("systemctl status docker", "查看 Docker 服务状态", "LOW", ""),
                new CommandSuggestion("groups", "查看当前用户所属用户组", "LOW", ""),
                new CommandSuggestion("sudo usermod -aG docker $USER", "将当前用户加入 docker 用户组", "MEDIUM", "docker 用户组权限较高，请确认用户可信。")
        );
        return new AnalyzeResponse(
                sessionId,
                messageId,
                null,
                "当前用户可能没有权限访问 Docker daemon socket。",
                List.of("当前用户不在 docker 用户组中", "Docker 服务未启动", "docker.sock 权限异常"),
                List.of("检查 Docker 服务状态", "检查当前用户所属用户组", "检查 /var/run/docker.sock 权限"),
                List.of("如果用户不在 docker 组中，可加入 docker 用户组", "重新登录终端后再次执行 docker ps"),
                commands,
                "MEDIUM",
                List.of("加入 docker 用户组会提升用户权限，请确认用户可信。"),
                false,
                false,
                "规范 Docker 用户组授权，避免给普通用户过高权限。",
                List.of()
        );
    }
    /**
     * 应用风险检测结果。
     * @param response response参数。
     * @param request request参数。
     * @return 处理结果。
     */

    private AnalyzeResponse applyRiskDetection(AnalyzeResponse response, AnalyzeRequest request) {
        var detection = riskDetectionService.detect(
                response.commands(),
                List.of(
                        blankToEmpty(request.description()),
                        blankToEmpty(request.logContent()),
                        blankToEmpty(request.commandOutput())
                )
        );
        var riskWarnings = new LinkedHashSet<String>();
        riskWarnings.addAll(response.riskWarnings() == null ? List.of() : response.riskWarnings());
        riskWarnings.addAll(detection.warnings());
        var riskLevel = RiskLevel.max(response.riskLevel(), detection.riskLevel());
        return new AnalyzeResponse(
                response.sessionId(),
                response.messageId(),
                response.resultId(),
                response.summary(),
                response.possibleCauses(),
                response.checkSteps(),
                response.fixSteps(),
                detection.commands(),
                riskLevel,
                List.copyOf(riskWarnings),
                response.needRestart(),
                response.dataRisk() || "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel),
                response.prevention(),
                response.needMoreInfo()
        );
    }
    /**
     * 构建发送给AI的用户内容。
     * @param request request参数。
     * @return 处理结果。
     */

    private String buildUserContent(AnalyzeRequest request) {
        return """
                标题：%s
                类型：%s
                环境：%s
                是否生产：%s

                故障描述：
                %s

                日志内容：
                %s

                命令输出：
                %s
                """.formatted(
                request.title(),
                blankToDash(request.category()),
                blankToDash(request.environment()),
                Boolean.TRUE.equals(request.isProduction()) ? "是" : "否",
                blankToDash(request.description()),
                blankToDash(request.logContent()),
                blankToDash(request.commandOutput())
        );
    }
    /**
     * 空值转换为短横线。
     * @param value value参数。
     * @return 处理结果。
     */

    private String blankToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
    /**
     * 空值转换为空字符串。
     * @param value value参数。
     * @return 处理结果。
     */

    private String blankToEmpty(String value) {
        return StringUtils.hasText(value) ? value : "";
    }
    /**
     * 转换为JSON字符串。
     * @param value value参数。
     * @return 处理结果。
     */

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "分析结果序列化失败");
        }
    }
}
