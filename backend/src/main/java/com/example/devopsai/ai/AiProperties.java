package com.example.devopsai.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * AiProperties配置属性类，负责承载外部化配置。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /**
     * AI服务提供商。
     */
    private String provider = "OpenAI";
    /**
     * AI接口风格。
     */
    private String apiStyle = "RESPONSES";
    /**
     * AI服务基础地址。
     */
    private String baseUrl = "https://api.nexustokenai.com";
    /**
     * AI模型名称。
     */
    private String model = "gpt-5.5";
    /**
     * AI接口访问密钥。
     */
    private String apiKey;
    /**
     * AI响应最大Token数。
     */
    private Integer maxTokens = 4096;
    /**
     * AI采样温度。
     */
    private Double temperature = 0.3;
    /**
     * AI请求超时时间，单位为秒。
     */
    private Integer timeoutSeconds = 60;
    /**
     * 获取AI服务提供商。
     * @return 处理结果。
     */

    public String getProvider() { return provider; }
    /**
     * 设置AI服务提供商。
     * @param provider provider参数。
     */
    public void setProvider(String provider) { this.provider = provider; }
    /**
     * 获取AI接口风格。
     * @return 处理结果。
     */
    public String getApiStyle() { return apiStyle; }
    /**
     * 设置AI接口风格。
     * @param apiStyle apiStyle参数。
     */
    public void setApiStyle(String apiStyle) { this.apiStyle = apiStyle; }
    /**
     * 获取AI服务基础地址。
     * @return 处理结果。
     */
    public String getBaseUrl() { return baseUrl; }
    /**
     * 设置AI服务基础地址。
     * @param baseUrl baseUrl参数。
     */
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    /**
     * 获取AI模型名称。
     * @return 处理结果。
     */
    public String getModel() { return model; }
    /**
     * 设置AI模型名称。
     * @param model model参数。
     */
    public void setModel(String model) { this.model = model; }
    /**
     * 获取AI接口访问密钥。
     * @return 处理结果。
     */
    public String getApiKey() { return apiKey; }
    /**
     * 设置AI接口访问密钥。
     * @param apiKey apiKey参数。
     */
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    /**
     * 获取AI响应最大Token数。
     * @return 处理结果。
     */
    public Integer getMaxTokens() { return maxTokens; }
    /**
     * 设置AI响应最大Token数。
     * @param maxTokens maxTokens参数。
     */
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    /**
     * 获取AI采样温度。
     * @return 处理结果。
     */
    public Double getTemperature() { return temperature; }
    /**
     * 设置AI采样温度。
     * @param temperature temperature参数。
     */
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    /**
     * 获取AI请求超时时间，单位为秒。
     * @return 处理结果。
     */
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    /**
     * 设置AI请求超时时间，单位为秒。
     * @param timeoutSeconds timeoutSeconds参数。
     */
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
