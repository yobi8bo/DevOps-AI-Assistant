package com.example.devopsai.common;
/**
 * ApiResponse响应对象，负责封装接口返回数据。
 * 
 * @author zhang
 * @date 2026-06-29
 */

public record ApiResponse<T>(Object code, String message, T data) {
    /**
     * 执行success处理逻辑。
     * @param data data参数。
     * @return 处理结果。
     */

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    /**
     * 执行success处理逻辑。
     * @param message message参数。
     * @param data data参数。
     * @return 处理结果。
     */

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    /**
     * 执行failure处理逻辑。
     * @param code code参数。
     * @param message message参数。
     * @return 处理结果。
     */

    public static <T> ApiResponse<T> failure(Object code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

