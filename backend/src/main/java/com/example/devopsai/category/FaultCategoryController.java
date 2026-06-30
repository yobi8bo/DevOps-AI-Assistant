package com.example.devopsai.category;

import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.category.dto.CategoryQuery;
import com.example.devopsai.category.dto.CreateCategoryRequest;
import com.example.devopsai.category.dto.UpdateCategoryRequest;
import com.example.devopsai.category.dto.UpdateCategoryStatusRequest;
import com.example.devopsai.category.vo.CategoryOption;
import com.example.devopsai.category.vo.CategorySummary;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/fault-categories")
public class FaultCategoryController {

    private final FaultCategoryService faultCategoryService;

    public FaultCategoryController(FaultCategoryService faultCategoryService) {
        this.faultCategoryService = faultCategoryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CategorySummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(faultCategoryService.list(new CategoryQuery(keyword, status, pageNum, pageSize)));
    }

    @GetMapping("/options")
    public ApiResponse<List<CategoryOption>> options() {
        return ApiResponse.success(faultCategoryService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategorySummary> get(@PathVariable Long id) {
        return ApiResponse.success(faultCategoryService.get(id));
    }

    @PostMapping
    public ApiResponse<CategorySummary> create(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", faultCategoryService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategorySummary> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", faultCategoryService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateCategoryStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        faultCategoryService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        faultCategoryService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }
}
