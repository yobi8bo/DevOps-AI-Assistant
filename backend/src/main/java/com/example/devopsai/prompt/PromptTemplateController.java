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
/**
 * PromptTemplateController控制器，负责处理对应模块的HTTP请求。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {

    /**
     * 提示词模板服务。
     */
    private final PromptTemplateService promptTemplateService;
    /**
     * 创建PromptTemplateController实例。
     * @param promptTemplateService promptTemplateService参数。
     */

    public PromptTemplateController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }
    /**
     * 分页查询列表。
     * @param keyword keyword参数。
     * @param category category参数。
     * @param status status参数。
     * @param pageNum pageNum参数。
     * @param pageSize pageSize参数。
     * @return 处理结果。
     */

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
    /**
     * 执行get处理逻辑。
     * @param id id参数。
     * @return 处理结果。
     */

    @GetMapping("/{id}")
    public ApiResponse<PromptTemplateDetail> get(@PathVariable Long id) {
        return ApiResponse.success(promptTemplateService.get(id));
    }
    /**
     * 执行create处理逻辑。
     * @param request request参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @PostMapping
    public ApiResponse<PromptTemplateDetail> create(
            @Valid @RequestBody SavePromptTemplateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", promptTemplateService.create(request, principal.getId()));
    }
    /**
     * 执行update处理逻辑。
     * @param id id参数。
     * @param request request参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @PutMapping("/{id}")
    public ApiResponse<PromptTemplateDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody SavePromptTemplateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", promptTemplateService.update(id, request, principal.getId()));
    }
    /**
     * 执行updateStatus处理逻辑。
     * @param id id参数。
     * @param request request参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        promptTemplateService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }
    /**
     * 设置对应属性值。
     * @param id id参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @PostMapping("/{id}/default")
    public ApiResponse<Boolean> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        promptTemplateService.setDefault(id, principal.getId());
        return ApiResponse.success("默认模板已更新", true);
    }
    /**
     * 执行delete处理逻辑。
     * @param id id参数。
     * @param principal principal参数。
     * @return 处理结果。
     */

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        promptTemplateService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }
    /**
     * UpdateStatusRequest请求对象，负责承载接口入参。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record UpdateStatusRequest(Integer status) {
    }
}
