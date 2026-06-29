package com.example.devopsai.common;

import java.util.List;

public record PageResponse<T>(
        List<T> records,
        long pageNum,
        long pageSize,
        long total,
        long pages
) {
}

