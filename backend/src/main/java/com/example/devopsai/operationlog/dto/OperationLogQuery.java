package com.example.devopsai.operationlog.dto;

public record OperationLogQuery(
        String keyword,
        String module,
        String action,
        Boolean success,
        long pageNum,
        long pageSize
) {
}
