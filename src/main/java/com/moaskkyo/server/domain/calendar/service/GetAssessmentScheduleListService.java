package com.moaskkyo.server.domain.calendar.service;

import com.moaskkyo.server.domain.assessment.domain.repository.AssessmentRepository;
import com.moaskkyo.server.domain.calendar.domain.repository.CalendarSettingRepository;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.AssessmentScheduleSummaryResponse;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.GetAssessmentScheduleListResponse;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAssessmentScheduleListService {

    private static final int MAX_RANGE_DAYS = 42;
    // 학년도는 3월 1일에 바뀐다. 1~2월은 전년도 학년도에 속한다.
    private static final Month SCHOOL_YEAR_START_MONTH = Month.MARCH;

    private final UserRepository userRepository;
    private final CalendarSettingRepository calendarSettingRepository;
    private final AssessmentRepository assessmentRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetAssessmentScheduleListResponse execute(Long userId, Role role, LocalDate startDate, LocalDate endDate) {
        // TODO: 선생님은 학년, 반이 없어 대상 수행평가를 정할 수 없다. 조회 기준이 정해지면 연결한다.
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
                .isShowAssessment();

        // 조회 범위가 아니라 오늘 기준의 학년도만 보여준다. 지난 학년도의 학년, 반 기록이 없기 때문이다.
        LocalDate today = LocalDate.now(clock);
        int schoolYear = today.getMonthValue() >= SCHOOL_YEAR_START_MONTH.getValue()
                ? today.getYear()
                : today.getYear() - 1;

        List<AssessmentScheduleSummaryResponse> assessments = !enabled
                ? List.of()
                : assessmentRepository.findAllByTargetClassAndPeriod(user.getSchool(), schoolYear,
                                user.getGrade(), user.getClassNumber(), startDate, endDate)
                        .stream()
                        .map(assessment -> AssessmentScheduleSummaryResponse.builder()
                                .assessmentId(assessment.getAssessmentId())
                                .subjectName(assessment.getSubject().getName())
                                .title(assessment.getTitle())
                                .startDate(assessment.getStartDate())
                                .endDate(assessment.getEndDate())
                                .build())
                        .toList();

        return GetAssessmentScheduleListResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .enabled(enabled)
                .assessments(assessments)
                .build();
    }
}
