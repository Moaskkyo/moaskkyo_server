package com.moaskkyo.server.domain.countdown.presentation;

import com.moaskkyo.server.domain.countdown.presentation.dto.response.CountdownListResponse;
import com.moaskkyo.server.domain.countdown.service.GetCountdownListService;
import com.moaskkyo.server.global.auth.JwtTokenProvider;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/countdowns")
@RequiredArgsConstructor
public class CountdownController {

    // TODO: 로그인 API가 없어 토큰 발급이 불가하다. 추가되면 실제 요청으로 응답을 확인한다.

    private final GetCountdownListService getCountdownListService;

    @GetMapping
    public ResponseEntity<CountdownListResponse> getCountdownList(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(getCountdownListService.execute(resolveSubjectId(jwt), resolveRole(jwt)));
    }

    private Long resolveSubjectId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException | NullPointerException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Role resolveRole(Jwt jwt) {
        try {
            return Role.valueOf(jwt.getClaimAsString(JwtTokenProvider.CLAIM_ROLE));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
