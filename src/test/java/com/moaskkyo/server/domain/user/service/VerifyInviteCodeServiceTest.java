package com.moaskkyo.server.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.school.domain.repository.SchoolRepository;
import com.moaskkyo.server.domain.user.presentation.dto.request.VerifyInviteCodeRequest;
import com.moaskkyo.server.domain.user.presentation.dto.response.VerifyInviteCodeResponse;
import com.moaskkyo.server.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyInviteCodeServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private VerifyInviteCodeService service;

    private School school() {
        return School.builder()
                .name("대덕소프트웨어마이스터고등학교")
                .atptCode("G10")
                .schoolCode("7430310")
                .joinCode("AB12CD")
                .build();
    }

    @Test
    @DisplayName("올바른 초대 코드면 학교 이름을 돌려준다")
    void success() {
        given(schoolRepository.findByJoinCode("AB12CD")).willReturn(Optional.of(school()));

        VerifyInviteCodeResponse response = service.execute(request("AB12CD"));

        assertThat(response.getSchoolName()).isEqualTo("대덕소프트웨어마이스터고등학교");
    }

    @Test
    @DisplayName("앞뒤 공백은 제거하고 확인한다")
    void trimsWhitespace() {
        given(schoolRepository.findByJoinCode("AB12CD")).willReturn(Optional.of(school()));

        VerifyInviteCodeResponse response = service.execute(request("  AB12CD "));

        assertThat(response.getSchoolName()).isEqualTo("대덕소프트웨어마이스터고등학교");
    }

    @Test
    @DisplayName("존재하지 않는 코드는 INVALID_INVITE_CODE")
    void notFound() {
        given(schoolRepository.findByJoinCode("ZZZZZZ")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(request("ZZZZZZ")))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    @DisplayName("6글자가 아니거나 비어 있으면 DB를 조회하지 않고 같은 오류로 응답한다")
    void invalidLength() {
        assertThatThrownBy(() -> service.execute(request("AB12C")))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);
        assertThatThrownBy(() -> service.execute(request("AB12CDE")))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);
        assertThatThrownBy(() -> service.execute(request("")))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);
        assertThatThrownBy(() -> service.execute(request(null)))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);

        verifyNoInteractions(schoolRepository);
    }

    private VerifyInviteCodeRequest request(String inviteCode) {
        return VerifyInviteCodeRequest.builder().inviteCode(inviteCode).build();
    }
}
