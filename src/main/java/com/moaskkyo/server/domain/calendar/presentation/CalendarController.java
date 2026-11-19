package com.moaskkyo.server.domain.calendar.presentation;

import com.moaskkyo.server.domain.calendar.presentation.dto.response.GetAcademicScheduleListResponse;
import com.moaskkyo.server.domain.calendar.service.GetAcademicScheduleListService;
import com.moaskkyo.server.global.auth.JwtTokenProvider;
import com.moaskkyo.server.global.auth.Role;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final GetAcademicScheduleListService getAcademicScheduleListService;

    @GetMapping("/academics")
    @ResponseStatus(HttpStatus.OK)
    public GetAcademicScheduleListResponse getAcademicScheduleList(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = Long.valueOf(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString(JwtTokenProvider.CLAIM_ROLE));
        return getAcademicScheduleListService.execute(userId, role, startDate, endDate);
    }
}
