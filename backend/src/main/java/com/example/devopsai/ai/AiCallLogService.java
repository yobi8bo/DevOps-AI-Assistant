package com.example.devopsai.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.devopsai.ai.entity.AiCallLog;
import com.example.devopsai.ai.mapper.AiCallLogMapper;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.model.ModelConfigService.ResolvedModelConfig;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiCallLogService {

    private final AiCallLogMapper aiCallLogMapper;

    public AiCallLogService(AiCallLogMapper aiCallLogMapper) {
        this.aiCallLogMapper = aiCallLogMapper;
    }

    public PageResponse<AiCallLogItem> list(LogQuery query) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(
            String requestId,
            Long userId,
            Long sessionId,
            ResolvedModelConfig modelConfig,
            AiClient.AiResponse response,
            long latencyMs
    ) {
        var log = baseLog(requestId, userId, sessionId, modelConfig, latencyMs);
        log.setSuccess(1);
        log.setPromptTokens(response.promptTokens());
        log.setCompletionTokens(response.completionTokens());
        log.setTotalTokens(response.totalTokens());
        aiCallLogMapper.insert(log);
    }

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
        log.setErrorMessage(errorMessage == null ? null : errorMessage.substring(0, Math.min(errorMessage.length(), 1000)));
        aiCallLogMapper.insert(log);
    }

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

    public record LogQuery(
            String keyword,
            Boolean success,
            Long modelConfigId,
            Long sessionId,
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long pageNum,
            long pageSize
    ) {
    }

    public record AiCallLogItem(
            Long id,
            String requestId,
            Long userId,
            Long sessionId,
            Long modelConfigId,
            String provider,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            Integer latencyMs,
            Boolean success,
            String errorCode,
            String errorMessage,
            LocalDateTime createdAt
    ) {
    }
}
