package com.example.devopsai.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        log.warn("business_exception code={} message={}", exception.getCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(ApiResponse.failure(exception.getErrorCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldError();
        var message = fieldError == null ? "请求参数错误" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        log.warn("validation_exception message={}", message);
        return ResponseEntity
                .status(ErrorCode.COMMON_PARAM_INVALID.getHttpStatus())
                .body(ApiResponse.failure(ErrorCode.COMMON_PARAM_INVALID, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("unhandled_exception", exception);
        return ResponseEntity
                .status(ErrorCode.COMMON_INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.failure(ErrorCode.COMMON_INTERNAL_ERROR));
    }
}
