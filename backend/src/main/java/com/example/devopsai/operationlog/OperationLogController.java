package com.example.devopsai.operationlog;

import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.operationlog.OperationLogService.OperationLogQuery;
import com.example.devopsai.operationlog.OperationLogService.OperationLogSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<OperationLogSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean success,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(operationLogService.list(
                new OperationLogQuery(keyword, module, action, success, pageNum, pageSize)
        ));
    }
}
