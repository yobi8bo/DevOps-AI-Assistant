package com.example.devopsai.diagnosis.vo;

import java.time.LocalDateTime;

public record MessageItem(
        Long id,
        String role,
        String content,
        LocalDateTime createdAt
) {
}
