package com.moaskkyo.server.global.error;

import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String UK_USER_PHONE_NUMBER = "uk_user_phone_number";
    public static final String UK_USER_SCHOOL_STUDENT = "uk_user_school_student";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        ErrorResponse body = ErrorResponse.builder()
                .code(code.name())
                .message(e.getMessage())
                .errors(e.getErrors())
                .build();
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldErrorDetail> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.name())
                .message(ErrorCode.VALIDATION_FAILED.getMessage())
                .errors(errors)
                .build();
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        String detail = rootMessage(e).toLowerCase(Locale.ROOT);
        if (detail.contains(UK_USER_PHONE_NUMBER)) {
            return toResponse(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        if (detail.contains(UK_USER_SCHOOL_STUDENT)) {
            return toResponse(ErrorCode.STUDENT_NUMBER_ALREADY_REGISTERED);
        }
        log.error("처리하지 못한 DB 무결성 오류", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(RuntimeException e) {
        if (e instanceof org.springframework.web.ErrorResponse) {
            throw e;
        }
        log.error("처리하지 못한 오류", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode code) {
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, code.getMessage()));
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? "" : root.getMessage();
    }
}
