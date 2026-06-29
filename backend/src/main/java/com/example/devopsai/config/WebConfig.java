package com.example.devopsai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * WebConfig配置类，负责声明对应模块的基础配置。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 注册跨域映射。
     * @param registry registry参数。
     */

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

