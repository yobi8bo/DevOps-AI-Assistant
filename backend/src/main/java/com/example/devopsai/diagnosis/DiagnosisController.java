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
/**
 * DiagnosisController控制器，负责处理对应模块的HTTP请求。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    /**
     * 诊断服务。
     */
    private final DiagnosisService diagnosisService;
    /**
     * 创建DiagnosisController实例。
     * @param diagnosisService diagnosisService参数。
     */

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }
    /**
     * 执行analyze处理逻辑。
     * @param request request参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @PostMapping("/analyze")
    public ApiResponse<AnalyzeResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("分析成功", diagnosisService.analyze(request, principal.getId()));
    }
    /**
     * 分页查询诊断会话。
     * @param principal principal参数。
     * @param keyword keyword参数。
     * @param category category参数。
     * @param status status参数。
     * @param isProduction isProduction参数。
     * @param pageNum pageNum参数。
     * @param pageSize pageSize参数。
     * @return 处理结果。
     */

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
    /**
     * 获取对应属性值。
     * @param id id参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @GetMapping("/sessions/{id}")
    public ApiResponse<SessionDetail> getSession(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(diagnosisService.getSession(id, principal.getId()));
    }
    /**
     * 执行updateStatus处理逻辑。
     * @param id id参数。
     * @param request request参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @PatchMapping("/sessions/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        diagnosisService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("更新成功", true);
    }
    /**
     * 执行deleteSession处理逻辑。
     * @param id id参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Boolean> deleteSession(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        diagnosisService.deleteSession(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }

    /**
     * 在已有排障会话中继续追问。
     * @param id 会话ID。
     * @param request 追问内容。
     * @param principal 当前登录用户。
     * @return 分析结果。
     */
    @PostMapping("/sessions/{id}/messages")
    public ApiResponse<AnalyzeResponse> continueAnalyze(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("分析成功", diagnosisService.continueAnalyze(id, request, principal.getId()));
    }
    /**
     * AnalyzeRequest请求对象，负责承载接口入参。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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

    /**
     * FollowUpRequest请求对象，负责承载继续追问入参。
     *
     * @author zhang
     * @date 2026-06-29
     */
    public record FollowUpRequest(
            @NotBlank String content,
            Long modelConfigId
    ) {
    }
    /**
     * AnalyzeResponse响应对象，负责封装接口返回数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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
    /**
     * CommandSuggestion数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record CommandSuggestion(
            String command,
            String description,
            String riskLevel,
            String warning
    ) {
    }
    /**
     * SessionQuery数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record SessionQuery(
            String keyword,
            String category,
            String status,
            Boolean isProduction,
            long pageNum,
            long pageSize
    ) {
    }
    /**
     * SessionSummary数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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
    /**
     * SessionDetail数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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
    /**
     * MessageItem数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record MessageItem(
            Long id,
            String role,
            String content,
            LocalDateTime createdAt
    ) {
    }
    /**
     * ResultItem数据传输对象，负责承载不可变数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

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
    /**
     * UpdateStatusRequest请求对象，负责承载接口入参。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record UpdateStatusRequest(@NotBlank String status) {
    }
}
