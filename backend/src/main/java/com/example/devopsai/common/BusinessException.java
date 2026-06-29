package com.example.devopsai.common;
/**
 * BusinessException异常类，负责表达业务异常场景。
 * 
 * @author zhang
 * @date 2026-06-29
 */

public class BusinessException extends RuntimeException {

    /**
     * 业务响应码。
     */
    private final Object code;
    /**
     * 创建BusinessException实例。
     * @param code code参数。
     * @param message message参数。
     */

    public BusinessException(Object code, String message) {
        super(message);
        this.code = code;
    }
    /**
     * 获取业务响应码。
     * @return 处理结果。
     */

    public Object getCode() {
        return code;
    }
}

