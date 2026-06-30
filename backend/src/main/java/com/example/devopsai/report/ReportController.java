package com.example.devopsai.report;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.report.ReportService.ReportDetail;
import com.example.devopsai.report.ReportService.ReportSummary;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/from-session/{sessionId}")
    public ApiResponse<ReportDetail> createFromSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("生成成功", reportService.createFromSession(sessionId, principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportDetail> get(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(reportService.get(id, principal.getId()));
    }

    @GetMapping
    public ApiResponse<List<ReportSummary>> list(
            @RequestParam(required = false) Long sessionId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(reportService.list(sessionId, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        reportService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }
}
