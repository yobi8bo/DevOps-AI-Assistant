package com.example.devopsai.common;

import java.util.List;
/**
 * PageResponse响应对象，负责封装接口返回数据。
 * 
 * @author zhang
 * @date 2026-06-29
 */

public record PageResponse<T>(
        List<T> records,
        long pageNum,
        long pageSize,
        long total,
        long pages
) {
}

