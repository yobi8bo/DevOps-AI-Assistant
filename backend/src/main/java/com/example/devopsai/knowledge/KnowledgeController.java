package com.example.devopsai.knowledge;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.knowledge.dto.KnowledgeQuery;
import com.example.devopsai.knowledge.dto.SaveKnowledgeFromCaseRequest;
import com.example.devopsai.knowledge.dto.SaveKnowledgeRequest;
import com.example.devopsai.knowledge.dto.UpdateKnowledgeStatusRequest;
import com.example.devopsai.knowledge.vo.KnowledgeDetail;
import com.example.devopsai.knowledge.vo.KnowledgeSummary;
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
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    public ApiResponse<PageResponse<KnowledgeSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(knowledgeService.list(new KnowledgeQuery(
                keyword,
                category,
                tag,
                status,
                pageNum,
                pageSize
        )));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDetail> get(@PathVariable Long id) {
        return ApiResponse.success(knowledgeService.get(id));
    }

    @PostMapping
    public ApiResponse<KnowledgeDetail> create(
            @Valid @RequestBody SaveKnowledgeRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", knowledgeService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveKnowledgeRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", knowledgeService.update(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        knowledgeService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateKnowledgeStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        knowledgeService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }

    @PostMapping("/from-case/{caseId}")
    public ApiResponse<KnowledgeDetail> createFromCase(
            @PathVariable Long caseId,
            @RequestBody(required = false) SaveKnowledgeFromCaseRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("转入知识库成功", knowledgeService.createFromCase(caseId, request, principal.getId()));
    }
}
