package com.moaskkyo.server.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "초대 코드가 올바르지 않아요. 선생님께 다시 확인해 주세요."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력한 정보를 확인해 주세요."),
    PHONE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 전화번호예요. 로그인해 주세요."),
    STUDENT_NUMBER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 학번이에요. 학년, 반, 번호를 확인해 주세요."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "전화번호 또는 비밀번호가 일치하지 않아요"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요해요."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "접근할 수 없어요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 생겼어요. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;
}
