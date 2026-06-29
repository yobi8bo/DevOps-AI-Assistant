package com.example.devopsai.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * GlobalExceptionHandler类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * 执行handleBusinessException处理逻辑。
     * @param exception exception参数。
     * @return 处理结果。
     */

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        log.warn("business_exception code={} message={}", exception.getCode(), exception.getMessage());
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
    }
    /**
     * 执行handleValidationException处理逻辑。
     * @param exception exception参数。
     * @return 处理结果。
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldError();
        var message = fieldError == null ? "请求参数错误" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        log.warn("validation_exception message={}", message);
        return ApiResponse.failure(400, message);
    }
    /**
     * 执行handleException处理逻辑。
     * @param exception exception参数。
     * @return 处理结果。
     */

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("unhandled_exception", exception);
        return ApiResponse.failure(500, "服务器内部错误");
    }
}
