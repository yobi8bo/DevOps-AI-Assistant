package com.example.devopsai.ai;

import com.example.devopsai.ai.AiCallLogService.AiCallLogItem;
import com.example.devopsai.ai.AiCallLogService.LogQuery;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * AiCallLogController控制器，负责处理对应模块的HTTP请求。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@RestController
@RequestMapping("/api/ai-call-logs")
public class AiCallLogController {

    /**
     * AI调用日志服务。
     */
    private final AiCallLogService aiCallLogService;
    /**
     * 创建AiCallLogController实例。
     * @param aiCallLogService aiCallLogService参数。
     */

    public AiCallLogController(AiCallLogService aiCallLogService) {
        this.aiCallLogService = aiCallLogService;
    }
    /**
     * 分页查询列表。
     * @param keyword keyword参数。
     * @param success success参数。
     * @param modelConfigId modelConfigId参数。
     * @param sessionId sessionId参数。
     * @param userId userId参数。
     * @param startTime startTime参数。
     * @param endTime endTime参数。
     * @param pageNum pageNum参数。
     * @param pageSize pageSize参数。
     * @return 处理结果。
     */

    @GetMapping
    public ApiResponse<PageResponse<AiCallLogItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) Long modelConfigId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(aiCallLogService.list(new LogQuery(
                keyword,
                success,
                modelConfigId,
                sessionId,
                userId,
                startTime,
                endTime,
                pageNum,
                pageSize
        )));
    }
}
