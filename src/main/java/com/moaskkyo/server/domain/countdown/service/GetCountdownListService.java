package com.moaskkyo.server.domain.countdown.service;

import com.moaskkyo.server.domain.countdown.domain.CountdownType;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.CountdownListResponse;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.CountdownResponse;
import com.moaskkyo.server.domain.exam.domain.repository.ExamRepository;
import com.moaskkyo.server.domain.hometime.domain.HomeTime;
import com.moaskkyo.server.domain.hometime.domain.repository.HomeTimeRepository;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.domain.vacation.domain.repository.VacationRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
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
    private final VacationRepository vacationRepository;
    private final ExamRepository examRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public CountdownListResponse execute(Long subjectId, Role role) {
        OffsetDateTime serverNow = OffsetDateTime.now(clock);
        LocalDate today = serverNow.toLocalDate();
        Long schoolId = resolveSchoolId(subjectId, role);

        return CountdownListResponse.builder()
                .serverNow(serverNow)
                .countdowns(List.of(
                        resolveHome(schoolId, today),
                        resolveVacation(schoolId, today),
                        resolveExam(schoolId, today)))
                .build();
    }

    private Long resolveSchoolId(Long subjectId, Role role) {
        // TODO: 선생님(어드민) 조회는 Teacher 엔티티가 추가되면 연결한다.
        if (role != Role.STUDENT) {
            throw new BusinessException(ErrorCode.SCHOOL_NOT_FOUND);
        }
        return userRepository.findById(subjectId)
                .map(user -> user.getSchool().getSchoolId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOOL_NOT_FOUND));
    }

    private CountdownResponse resolveHome(Long schoolId, LocalDate today) {
        return homeTimeRepository.findBySchoolSchoolIdAndDate(schoolId, today)
                .map(HomeTime::getDismissalTime)
                .filter(Objects::nonNull)
                .map(dismissalTime -> countdown(CountdownType.HOME, HOME_TITLE, today.atTime(dismissalTime)))
                .orElseGet(() -> CountdownResponse.empty(CountdownType.HOME, HOME_TITLE));
    }

    private CountdownResponse resolveVacation(Long schoolId, LocalDate today) {
        return vacationRepository
                .findFirstBySchoolSchoolIdAndStartDateGreaterThanEqualOrderByStartDateAsc(schoolId, today)
                .map(vacation -> countdown(
                        CountdownType.VACATION, vacation.getTitle(), vacation.getStartDate().atStartOfDay()))
                .orElseGet(() -> CountdownResponse.empty(CountdownType.VACATION, VACATION_TITLE));
    }

    private CountdownResponse resolveExam(Long schoolId, LocalDate today) {
        // 시험까지 남은 시간은 학년, 학과와 무관하게 같으므로 학교에서 가장 빠른 시험을 쓴다.
        return examRepository.findFirstBySchoolSchoolIdAndDateGreaterThanEqualOrderByDateAscPeriodAsc(schoolId, today)
                .map(exam -> countdown(CountdownType.EXAM, exam.getType().getTitle(), exam.getDate().atStartOfDay()))
                .orElseGet(() -> CountdownResponse.empty(CountdownType.EXAM, EXAM_TITLE));
    }

    private CountdownResponse countdown(CountdownType type, String title, LocalDateTime targetAt) {
        return CountdownResponse.builder()
                .type(type)
                .title(title)
                .targetAt(targetAt.atZone(clock.getZone()).toOffsetDateTime())
                .build();
    }
}
