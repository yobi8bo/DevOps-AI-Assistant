package com.example.devopsai.diagnosis;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.diagnosis.dto.AnalyzeRequest;
import com.example.devopsai.diagnosis.dto.FollowUpRequest;
import com.example.devopsai.diagnosis.dto.ReanalyzeRequest;
import com.example.devopsai.diagnosis.dto.SessionQuery;
import com.example.devopsai.diagnosis.dto.UpdateStatusRequest;
import com.example.devopsai.diagnosis.vo.AnalyzeResponse;
import com.example.devopsai.diagnosis.vo.SessionDetail;
import com.example.devopsai.diagnosis.vo.SessionSummary;
import jakarta.validation.Valid;
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

    @PostMapping("/sessions/{id}/reanalyze")
    public ApiResponse<AnalyzeResponse> reanalyze(
            @PathVariable Long id,
            @RequestBody(required = false) ReanalyzeRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("分析成功", diagnosisService.reanalyze(id, request, principal.getId()));
    }
}
