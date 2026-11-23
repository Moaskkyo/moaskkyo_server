package com.moaskkyo.server.domain.countdown.presentation;

import com.moaskkyo.server.domain.countdown.presentation.dto.response.GetCountdownListResponse;
import com.moaskkyo.server.domain.countdown.service.GetCountdownListService;
import com.moaskkyo.server.global.auth.JwtTokenProvider;
import com.moaskkyo.server.global.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/countdowns")
@RequiredArgsConstructor
public class CountdownController {

    private final GetCountdownListService getCountdownListService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetCountdownListResponse getCountdownList(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString(JwtTokenProvider.CLAIM_ROLE));
        return getCountdownListService.execute(userId, role);
    }
}
