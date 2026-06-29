package com.example.devopsai.common;

public class BusinessException extends RuntimeException {

    private final Object code;

    public BusinessException(Object code, String message) {
        super(message);
        this.code = code;
    }

    public Object getCode() {
        return code;
    }
}

