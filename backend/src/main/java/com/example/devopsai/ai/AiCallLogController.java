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

@RestController
@RequestMapping("/api/ai-call-logs")
public class AiCallLogController {

    private final AiCallLogService aiCallLogService;

    public AiCallLogController(AiCallLogService aiCallLogService) {
        this.aiCallLogService = aiCallLogService;
    }

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
