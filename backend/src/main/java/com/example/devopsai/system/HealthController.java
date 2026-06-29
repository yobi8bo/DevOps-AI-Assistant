package com.example.devopsai.system;

import com.example.devopsai.common.ApiResponse;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * HealthController控制器，负责处理对应模块的HTTP请求。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@RestController
@RequestMapping("/api/health")
public class HealthController {
    /**
     * 获取系统健康状态。
     * @return 处理结果。
     */

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "devops-ai-assistant",
                "time", OffsetDateTime.now().toString()
        ));
    }
}

