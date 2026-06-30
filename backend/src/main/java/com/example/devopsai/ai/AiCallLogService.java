package com.example.devopsai.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.ai.dto.AiCallLogQuery;
import com.example.devopsai.ai.entity.AiCallLog;
import com.example.devopsai.ai.mapper.AiCallLogMapper;
import com.example.devopsai.ai.vo.AiResponse;
import com.example.devopsai.ai.vo.AiCallLogItem;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.model.dto.ResolvedModelConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/**
 * AiCallLogService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class AiCallLogService {

    /**
     * AI调用日志数据访问对象。
     */
    private final AiCallLogMapper aiCallLogMapper;
    /**
     * 创建AiCallLogService实例。
     * @param aiCallLogMapper aiCallLogMapper参数。
     */

    public AiCallLogService(AiCallLogMapper aiCallLogMapper) {
        this.aiCallLogMapper = aiCallLogMapper;
    }
    /**
     * 执行list处理逻辑。
     * @param query query参数。
     * @return 处理结果。
     */

    public PageResponse<AiCallLogItem> list(AiCallLogQuery query) {
        var wrapper = new LambdaQueryWrapper<AiCallLog>()
                .orderByDesc(AiCallLog::getCreatedAt);
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(AiCallLog::getRequestId, query.keyword())
                    .or()
                    .like(AiCallLog::getProvider, query.keyword())
                    .or()
                    .like(AiCallLog::getModelName, query.keyword())
                    .or()
                    .like(AiCallLog::getErrorMessage, query.keyword()));
        }
        if (query.success() != null) {
            wrapper.eq(AiCallLog::getSuccess, query.success() ? 1 : 0);
        }
        if (query.modelConfigId() != null) {
            wrapper.eq(AiCallLog::getModelConfigId, query.modelConfigId());
        }
        if (query.sessionId() != null) {
            wrapper.eq(AiCallLog::getSessionId, query.sessionId());
        }
        if (query.userId() != null) {
            wrapper.eq(AiCallLog::getUserId, query.userId());
        }
        if (query.startTime() != null) {
            wrapper.ge(AiCallLog::getCreatedAt, query.startTime());
        }
        if (query.endTime() != null) {
            wrapper.le(AiCallLog::getCreatedAt, query.endTime());
        }

        IPage<AiCallLog> page = aiCallLogMapper.selectPage(new Page<>(query.pageNum(), query.pageSize()), wrapper);
        var records = page.getRecords().stream().map(this::toItem).toList();
        return new PageResponse<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }
    /**
     * 执行logSuccess处理逻辑。
     * @param requestId requestId参数。
     * @param userId userId参数。
     * @param sessionId sessionId参数。
     * @param modelConfig modelConfig参数。
     * @param response response参数。
     * @param latencyMs latencyMs参数。
     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(
            String requestId,
            Long userId,
            Long sessionId,
            ResolvedModelConfig modelConfig,
            AiResponse response,
            long latencyMs
    ) {
        var log = baseLog(requestId, userId, sessionId, modelConfig, latencyMs);
        log.setSuccess(1);
        log.setPromptTokens(response.promptTokens());
        log.setCompletionTokens(response.completionTokens());
        log.setTotalTokens(response.totalTokens());
        aiCallLogMapper.insert(log);
    }
    /**
     * 执行logFailure处理逻辑。
     * @param requestId requestId参数。
     * @param userId userId参数。
     * @param sessionId sessionId参数。
     * @param modelConfig modelConfig参数。
     * @param errorCode errorCode参数。
     * @param errorMessage errorMessage参数。
     * @param latencyMs latencyMs参数。
     */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(
            String requestId,
            Long userId,
            Long sessionId,
            ResolvedModelConfig modelConfig,
            String errorCode,
            String errorMessage,
            long latencyMs
    ) {
        var log = baseLog(requestId, userId, sessionId, modelConfig, latencyMs);
        log.setSuccess(0);
        log.setErrorCode(errorCode);
        log.setErrorMessage(errorMessage == null
                ? null
                : errorMessage.substring(0, Math.min(errorMessage.length(), 1000)));
        aiCallLogMapper.insert(log);
    }
    /**
     * 执行baseLog处理逻辑。
     * @param requestId requestId参数。
     * @param userId userId参数。
     * @param sessionId sessionId参数。
     * @param modelConfig modelConfig参数。
     * @param latencyMs latencyMs参数。
     * @return 处理结果。
     */

    private AiCallLog baseLog(
            String requestId,
            Long userId,
            Long sessionId,
            ResolvedModelConfig modelConfig,
            long latencyMs
    ) {
        var log = new AiCallLog();
        log.setRequestId(requestId);
        log.setUserId(userId);
        log.setSessionId(sessionId);
        log.setModelConfigId(modelConfig.id());
        log.setProvider(modelConfig.provider());
        log.setModelName(modelConfig.model());
        log.setLatencyMs((int) Math.min(latencyMs, Integer.MAX_VALUE));
        return log;
    }
    /**
     * 转换业务数据视图。
     * @param log log参数。
     * @return 处理结果。
     */

    private AiCallLogItem toItem(AiCallLog log) {
        return new AiCallLogItem(
                log.getId(),
                log.getRequestId(),
                log.getUserId(),
                log.getSessionId(),
                log.getModelConfigId(),
                log.getProvider(),
                log.getModelName(),
                log.getPromptTokens(),
                log.getCompletionTokens(),
                log.getTotalTokens(),
                log.getLatencyMs(),
                Integer.valueOf(1).equals(log.getSuccess()),
                log.getErrorCode(),
                log.getErrorMessage(),
                log.getCreatedAt()
        );
    }
    /**
     * LogQuery数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

}
