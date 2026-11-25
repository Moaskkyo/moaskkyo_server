package com.moaskkyo.server.domain.countdown.service;

import com.moaskkyo.server.domain.countdown.domain.CountdownType;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.CountdownSummaryResponse;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.GetCountdownListResponse;
import com.moaskkyo.server.domain.hometime.domain.HomeTime;
import com.moaskkyo.server.domain.hometime.domain.repository.HomeTimeRepository;
import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.teacher.domain.Teacher;
import com.moaskkyo.server.domain.teacher.domain.repository.TeacherRepository;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCountdownListService {

    private static final String HOME_TITLE = "하교";

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final HomeTimeRepository homeTimeRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetCountdownListResponse execute(Long userId, Role role) {
        School school = resolveSchool(userId, role);
        // 1초 단위로 갱신하는 화면이라 그보다 작은 단위는 버린다.
        OffsetDateTime serverNow = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);

        return GetCountdownListResponse.builder()
                .serverNow(serverNow)
                .countdowns(List.of(resolveHome(school.getSchoolId(), serverNow.toLocalDate())))
                .build();
    }

    private School resolveSchool(Long userId, Role role) {
        if (role == Role.TEACHER) {
            return teacherRepository.findWithSchoolByTeacherId(userId)
                    .map(Teacher::getSchool)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }
        return userRepository.findWithSchoolByUserId(userId)
                .map(User::getSchool)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 하교 시각이 지나도 당일에는 그대로 내려주고, 날짜가 바뀌면 그날의 하교 시각으로 바뀐다.
    private CountdownSummaryResponse resolveHome(Long schoolId, LocalDate today) {
        LocalTime dismissalTime = homeTimeRepository.findBySchoolSchoolIdAndDate(schoolId, today)
                .map(HomeTime::getDismissalTime)
                .orElse(null);
        return CountdownSummaryResponse.builder()
                .type(CountdownType.HOME)
                .title(HOME_TITLE)
                .targetAt(dismissalTime == null
                        ? null
                        : today.atTime(dismissalTime).atZone(clock.getZone()).toOffsetDateTime())
                .build();
    }
}
