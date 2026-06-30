package com.example.devopsai.casebase;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.casebase.CaseService.CaseDetail;
import com.example.devopsai.casebase.CaseService.CaseQuery;
import com.example.devopsai.casebase.CaseService.CaseSummary;
import com.example.devopsai.casebase.CaseService.SaveCaseFromSessionRequest;
import com.example.devopsai.casebase.CaseService.SaveCaseRequest;
import com.example.devopsai.casebase.CaseService.UpdateCaseStatusRequest;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CaseSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(caseService.list(new CaseQuery(keyword, category, status, tag, pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CaseDetail> get(@PathVariable Long id) {
        return ApiResponse.success(caseService.get(id));
    }

    @PostMapping
    public ApiResponse<CaseDetail> create(
            @Valid @RequestBody SaveCaseRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", caseService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CaseDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveCaseRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", caseService.update(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        caseService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }

    @PostMapping("/from-session/{sessionId}")
    public ApiResponse<CaseDetail> createFromSession(
            @PathVariable Long sessionId,
            @RequestBody(required = false) SaveCaseFromSessionRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存案例成功", caseService.createFromSession(sessionId, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCaseStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        caseService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }
}
