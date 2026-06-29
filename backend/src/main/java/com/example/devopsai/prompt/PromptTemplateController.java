package com.example.devopsai.prompt;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.prompt.PromptTemplateService.PromptTemplateDetail;
import com.example.devopsai.prompt.PromptTemplateService.PromptTemplateSummary;
import com.example.devopsai.prompt.PromptTemplateService.SavePromptTemplateRequest;
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
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    public PromptTemplateController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PromptTemplateSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(promptTemplateService.list(keyword, category, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<PromptTemplateDetail> get(@PathVariable Long id) {
        return ApiResponse.success(promptTemplateService.get(id));
    }

    @PostMapping
    public ApiResponse<PromptTemplateDetail> create(
            @Valid @RequestBody SavePromptTemplateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", promptTemplateService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<PromptTemplateDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody SavePromptTemplateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", promptTemplateService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        promptTemplateService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }

    @PostMapping("/{id}/default")
    public ApiResponse<Boolean> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        promptTemplateService.setDefault(id, principal.getId());
        return ApiResponse.success("默认模板已更新", true);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        promptTemplateService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }

    public record UpdateStatusRequest(Integer status) {
    }
}
