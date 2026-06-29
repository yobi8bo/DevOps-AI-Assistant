package com.example.devopsai.diagnosis;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/analyze")
    public ApiResponse<AnalyzeResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("分析成功", diagnosisService.analyze(request, principal.getId()));
    }

    @GetMapping("/sessions")
    public ApiResponse<PageResponse<SessionSummary>> listSessions(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isProduction,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        var query = new SessionQuery(keyword, category, status, isProduction, pageNum, pageSize);
        return ApiResponse.success(diagnosisService.listSessions(query, principal.getId()));
    }

    @GetMapping("/sessions/{id}")
    public ApiResponse<SessionDetail> getSession(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(diagnosisService.getSession(id, principal.getId()));
    }

    @PatchMapping("/sessions/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        diagnosisService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("更新成功", true);
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Boolean> deleteSession(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        diagnosisService.deleteSession(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }

    public record AnalyzeRequest(
            @NotBlank String title,
            String category,
            String environment,
            String osInfo,
            String middleware,
            String serviceType,
            Boolean isProduction,
            String urgencyLevel,
            String description,
            String logContent,
            String commandOutput,
            Long modelConfigId
    ) {
    }

    public record AnalyzeResponse(
            Long sessionId,
            Long messageId,
            Long resultId,
            String summary,
            List<String> possibleCauses,
            List<String> checkSteps,
            List<String> fixSteps,
            List<CommandSuggestion> commands,
            String riskLevel,
            List<String> riskWarnings,
            boolean needRestart,
            boolean dataRisk,
            String prevention,
            List<String> needMoreInfo
    ) {
    }

    public record CommandSuggestion(
            String command,
            String description,
            String riskLevel,
            String warning
    ) {
    }

    public record SessionQuery(
            String keyword,
            String category,
            String status,
            Boolean isProduction,
            long pageNum,
            long pageSize
    ) {
    }

    public record SessionSummary(
            Long id,
            String title,
            String category,
            String environment,
            Boolean isProduction,
            String urgencyLevel,
            String status,
            String riskLevel,
            String summary,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record SessionDetail(
            Long id,
            String title,
            String category,
            String environment,
            Boolean isProduction,
            String urgencyLevel,
            String status,
            List<MessageItem> messages,
            ResultItem latestResult,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record MessageItem(
            Long id,
            String role,
            String content,
            LocalDateTime createdAt
    ) {
    }

    public record ResultItem(
            Long id,
            String summary,
            String riskLevel,
            String resultJson,
            Long modelConfigId,
            Long promptTemplateId,
            String promptVersion,
            LocalDateTime createdAt
    ) {
    }

    public record UpdateStatusRequest(@NotBlank String status) {
    }
}
