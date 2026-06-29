package com.example.devopsai.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
/**
 * AiConfig配置类，负责声明对应模块的基础配置。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {
    /**
     * 执行restClientBuilder处理逻辑。
     * @return 处理结果。
     */

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
