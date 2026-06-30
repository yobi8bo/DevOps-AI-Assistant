package com.example.devopsai.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    SUCCESS("SUCCESS", "success", HttpStatus.OK),
    COMMON_PARAM_INVALID("COMMON_PARAM_INVALID", "请求参数错误", HttpStatus.BAD_REQUEST),
    COMMON_NOT_FOUND("COMMON_NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    COMMON_CONFLICT("COMMON_CONFLICT", "资源状态冲突", HttpStatus.CONFLICT),
    COMMON_INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED", "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN("AUTH_FORBIDDEN", "无权限访问", HttpStatus.FORBIDDEN),
    AI_PROVIDER_CALL_FAILED("AI_PROVIDER_CALL_FAILED", "AI 调用失败", HttpStatus.BAD_GATEWAY),
    AI_RESPONSE_INVALID("AI_RESPONSE_INVALID", "AI 响应格式错误", HttpStatus.BAD_GATEWAY),
    DIAGNOSIS_SESSION_NOT_FOUND("DIAGNOSIS_SESSION_NOT_FOUND", "排障会话不存在", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public static ErrorCode fromLegacyStatus(Object code) {
        if (!(code instanceof Number number)) {
            return COMMON_INTERNAL_ERROR;
        }
        return switch (number.intValue()) {
            case 400 -> COMMON_PARAM_INVALID;
            case 401 -> AUTH_UNAUTHORIZED;
            case 403 -> AUTH_FORBIDDEN;
            case 404 -> COMMON_NOT_FOUND;
            case 409 -> COMMON_CONFLICT;
            case 502 -> AI_PROVIDER_CALL_FAILED;
            default -> COMMON_INTERNAL_ERROR;
        };
    }
}
