package com.gov.common.exception;

public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public static BusinessException of(String message) {
        return new BusinessException(400, message);
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    public int getCode() { return code; }
    @Override public String getMessage() { return message; }
}
