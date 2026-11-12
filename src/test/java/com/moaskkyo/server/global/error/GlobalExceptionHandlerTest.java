package com.moaskkyo.server.global.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("전화번호 UNIQUE 위반은 409 PHONE_ALREADY_REGISTERED")
    void phoneUniqueViolation() {
        DataIntegrityViolationException e = new DataIntegrityViolationException("저장 실패",
                new RuntimeException("Duplicate entry '01012345678' for key 'user.uk_user_phone_number'"));

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("PHONE_ALREADY_REGISTERED");
    }

    @Test
    @DisplayName("학번 UNIQUE 위반은 409 STUDENT_NUMBER_ALREADY_REGISTERED (제약 이름은 대소문자 무관)")
    void studentNumberUniqueViolation() {
        DataIntegrityViolationException e = new DataIntegrityViolationException("저장 실패",
                new RuntimeException("Unique index or primary key violation: \"UK_USER_SCHOOL_STUDENT_INDEX_8\""));

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("STUDENT_NUMBER_ALREADY_REGISTERED");
    }

    @Test
    @DisplayName("그 밖의 무결성 오류는 500 INTERNAL_SERVER_ERROR")
    void otherIntegrityViolation() {
        DataIntegrityViolationException e = new DataIntegrityViolationException("저장 실패",
                new RuntimeException("Column 'name' cannot be null"));

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500 INTERNAL_SERVER_ERROR")
    void unexpected() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("Spring이 상태를 정해 둔 예외(404 등)는 500으로 바꾸지 않고 그대로 넘긴다")
    void springErrorResponsePassesThrough() {
        ResponseStatusException e = new ResponseStatusException(HttpStatus.NOT_FOUND);

        assertThatThrownBy(() -> handler.handleUnexpected(e)).isSameAs(e);
    }

    @Test
    @DisplayName("BusinessException은 오류 코드의 상태와 지정한 문구로 응답한다")
    void business() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                new BusinessException(ErrorCode.VALIDATION_FAILED, "토큰을 확인해 주세요."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getMessage()).isEqualTo("토큰을 확인해 주세요.");
        assertThat(response.getBody().getErrors()).isNull();
    }
}
