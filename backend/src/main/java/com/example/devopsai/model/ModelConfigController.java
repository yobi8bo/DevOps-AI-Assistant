package com.example.devopsai.model;

import com.example.devopsai.ai.AiCallLogService;
import com.example.devopsai.ai.AiClient;
import com.example.devopsai.auth.AppUserPrincipal;
import com.example.devopsai.common.ApiResponse;
import com.example.devopsai.common.PageResponse;
import com.example.devopsai.model.ModelConfigService.ModelConfigDetail;
import com.example.devopsai.model.ModelConfigService.ModelConfigSummary;
import com.example.devopsai.model.ModelConfigService.SaveModelConfigRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
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
@RequestMapping("/api/model-configs")
public class ModelConfigController {

    private final ModelConfigService modelConfigService;
    private final AiClient aiClient;
    private final AiCallLogService aiCallLogService;

    public ModelConfigController(
            ModelConfigService modelConfigService,
            AiClient aiClient,
            AiCallLogService aiCallLogService
    ) {
        this.modelConfigService = modelConfigService;
        this.aiClient = aiClient;
        this.aiCallLogService = aiCallLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ModelConfigSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(modelConfigService.list(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ModelConfigDetail> get(@PathVariable Long id) {
        return ApiResponse.success(modelConfigService.get(id));
    }

    @PostMapping
    public ApiResponse<ModelConfigDetail> create(
            @Valid @RequestBody SaveModelConfigRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("创建成功", modelConfigService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ModelConfigDetail> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveModelConfigRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success("保存成功", modelConfigService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        modelConfigService.updateStatus(id, request.status(), principal.getId());
        return ApiResponse.success("状态已更新", true);
    }

    @PostMapping("/{id}/default")
    public ApiResponse<Boolean> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        modelConfigService.setDefault(id, principal.getId());
        return ApiResponse.success("默认模型已更新", true);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<TestConnectionResponse> test(
            @PathVariable Long id,
            @RequestBody(required = false) TestConnectionRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        var modelConfig = modelConfigService.resolveForTest(id, request == null ? null : request.apiKey());
        var requestId = UUID.randomUUID().toString();
        var startedAt = Instant.now();
        try {
            var response = aiClient.createResponse(
                    modelConfig,
                    "你是连通性测试助手，只返回 JSON。",
                    "返回一个最小 JSON：{\"summary\":\"ok\",\"possibleCauses\":[],\"checkSteps\":[],\"fixSteps\":[],\"commands\":[],\"riskLevel\":\"LOW\",\"riskWarnings\":[],\"needRestart\":false,\"dataRisk\":false,\"prevention\":\"\",\"needMoreInfo\":[]}"
            );
            var latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
            aiCallLogService.logSuccess(requestId, principal.getId(), null, modelConfig, response, latencyMs);
            return ApiResponse.success("连接成功", new TestConnectionResponse(true, "连接成功", latencyMs));
        } catch (RuntimeException exception) {
            var latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
            aiCallLogService.logFailure(requestId, principal.getId(), null, modelConfig, "TEST_FAILED", exception.getMessage(), latencyMs);
            throw exception;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        modelConfigService.delete(id, principal.getId());
        return ApiResponse.success("删除成功", true);
    }

    public record UpdateStatusRequest(Integer status) {
    }

    public record TestConnectionRequest(String apiKey) {
    }

    public record TestConnectionResponse(
            Boolean success,
            String message,
            Long latencyMs
    ) {
    }
}
