package com.example.devopsai.model.vo;

public record TestConnectionResponse(
        Boolean success,
        String message,
        Long latencyMs
) {
}
