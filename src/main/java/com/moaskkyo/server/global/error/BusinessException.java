package com.moaskkyo.server.global.error;

import java.util.List;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ErrorResponse.FieldErrorDetail> errors;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public BusinessException(ErrorCode errorCode, String message, List<ErrorResponse.FieldErrorDetail> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }
}
