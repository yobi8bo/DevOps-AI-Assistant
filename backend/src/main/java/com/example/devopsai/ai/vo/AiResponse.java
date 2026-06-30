package com.example.devopsai.ai.vo;

public record AiResponse(
        String text,
        String rawResponse,
        int promptTokens,
        int completionTokens
) {
    public int totalTokens() {
        return promptTokens + completionTokens;
    }
}
