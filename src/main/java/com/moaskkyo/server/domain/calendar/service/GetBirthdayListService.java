package com.moaskkyo.server.domain.calendar.service;

import com.moaskkyo.server.domain.calendar.domain.repository.CalendarSettingRepository;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.BirthdaySummaryResponse;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.GetBirthdayListResponse;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBirthdayListService {

    private static final int MAX_RANGE_DAYS = 42;

    private final UserRepository userRepository;
    private final CalendarSettingRepository calendarSettingRepository;

    @Transactional(readOnly = true)
    public GetBirthdayListResponse execute(Long userId, Role role, LocalDate startDate, LocalDate endDate) {
        if (role != Role.STUDENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (startDate.isAfter(endDate) || ChronoUnit.DAYS.between(startDate, endDate) >= MAX_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        User user = userRepository.findWithSchoolByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        boolean enabled = calendarSettingRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.CALENDAR_SETTING_NOT_FOUND))
                .isShowBirthday();

        // 조회 범위가 연말을 걸치면 두 해의 생일 날짜를 모두 만들어 범위 안에 드는 것만 남긴다.
        List<BirthdaySummaryResponse> birthdays = !enabled
                ? List.of()
                : userRepository.findAllBySchoolAndBirthdayPublic(user.getSchool())
                        .stream()
                        .flatMap(student -> IntStream.rangeClosed(startDate.getYear(), endDate.getYear())
                                .mapToObj(student::birthdayIn)
                                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                                .map(date -> BirthdaySummaryResponse.builder()
                                        .userId(student.getUserId())
                                        .name(student.getName())
                                        .grade(student.getGrade())
                                        .classNumber(student.getClassNumber())
                                        .studentNumber(student.getStudentNumber())
                                        .date(date)
                                        .build()))
                        .sorted(Comparator.comparing(BirthdaySummaryResponse::getDate)
                                .thenComparing(BirthdaySummaryResponse::getGrade)
                                .thenComparing(BirthdaySummaryResponse::getClassNumber)
                                .thenComparing(BirthdaySummaryResponse::getStudentNumber))
                        .toList();

        return GetBirthdayListResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .enabled(enabled)
                .birthdays(birthdays)
                .build();
    }
}
