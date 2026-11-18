package com.moaskkyo.server.domain.countdown.service;

import com.moaskkyo.server.domain.calendar.domain.AcademicType;
import com.moaskkyo.server.domain.calendar.domain.repository.AcademicRepository;
import com.moaskkyo.server.domain.countdown.domain.CountdownType;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.CountdownSummaryResponse;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.GetCountdownListResponse;
import com.moaskkyo.server.domain.exam.domain.repository.ExamRepository;
import com.moaskkyo.server.domain.hometime.domain.HomeTime;
import com.moaskkyo.server.domain.hometime.domain.repository.HomeTimeRepository;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCountdownListService {

    private static final String HOME_TITLE = "하교";
    // TODO: 일정이 없을 때 쓰는 기본 제목. 화면에 보일 문구라 기획 확인이 필요하다.
    private static final String VACATION_TITLE = "방학";
    private static final String EXAM_TITLE = "시험";

    private final UserRepository userRepository;
    private final HomeTimeRepository homeTimeRepository;
    private final AcademicRepository academicRepository;
    private final ExamRepository examRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetCountdownListResponse execute(Long userId, Role role) {
        // TODO: 선생님 조회는 선생님 계정이 생기면 연결한다.
        if (role != Role.STUDENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findWithSchoolByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long schoolId = user.getSchool().getSchoolId();

        OffsetDateTime serverNow = OffsetDateTime.now(clock);
        LocalDate today = serverNow.toLocalDate();

        return GetCountdownListResponse.builder()
                .serverNow(serverNow)
                .countdowns(List.of(
                        resolveHome(schoolId, today),
                        resolveVacation(schoolId, today),
                        resolveExam(schoolId, today)))
                .build();
    }

    // 하교 시각이 지나도 당일에는 그대로 내려주고, 날짜가 바뀌면 그날의 하교 시각으로 바뀐다.
    private CountdownSummaryResponse resolveHome(Long schoolId, LocalDate today) {
        LocalTime dismissalTime = homeTimeRepository.findBySchoolSchoolIdAndDate(schoolId, today)
                .map(HomeTime::getDismissalTime)
                .orElse(null);
        return countdown(CountdownType.HOME, HOME_TITLE,
                dismissalTime == null ? null : today.atTime(dismissalTime));
    }

    private CountdownSummaryResponse resolveVacation(Long schoolId, LocalDate today) {
        return academicRepository
                .findFirstBySchoolSchoolIdAndTypeAndStartDateGreaterThanEqualOrderByStartDateAsc(
                        schoolId, AcademicType.VACATION, today)
                .map(academic -> countdown(
                        CountdownType.VACATION, academic.getTitle(), academic.getStartDate().atStartOfDay()))
                .orElseGet(() -> countdown(CountdownType.VACATION, VACATION_TITLE, null));
    }

    // 시험까지 남은 시간은 학년, 학과와 무관하게 같으므로 학교에서 가장 빠른 시험을 쓴다.
    private CountdownSummaryResponse resolveExam(Long schoolId, LocalDate today) {
        return examRepository.findFirstBySchoolSchoolIdAndDateGreaterThanEqualOrderByDateAscPeriodAsc(schoolId, today)
                .map(exam -> countdown(
                        CountdownType.EXAM, exam.getType().getTitle(), exam.getDate().atStartOfDay()))
                .orElseGet(() -> countdown(CountdownType.EXAM, EXAM_TITLE, null));
    }

    private CountdownSummaryResponse countdown(CountdownType type, String title, LocalDateTime targetAt) {
        return CountdownSummaryResponse.builder()
                .type(type)
                .title(title)
                .targetAt(targetAt == null ? null : targetAt.atZone(clock.getZone()).toOffsetDateTime())
                .build();
    }
}
