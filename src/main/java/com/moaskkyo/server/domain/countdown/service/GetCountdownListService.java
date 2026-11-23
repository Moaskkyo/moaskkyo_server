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
import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCountdownListService {

    private static final String HOME_TITLE = "하교";
    private static final String VACATION_TITLE = "방학";
    private static final String SUMMER_VACATION_TITLE = "여름방학";
    private static final String WINTER_VACATION_TITLE = "겨울방학";
    private static final String EXAM_TITLE = "시험";
    private static final String MIDTERM_TITLE = "중간고사";
    private static final String FINAL_TITLE = "기말고사";
    private static final String WRITTEN_TEST_TITLE = "지필평가";

    // 해를 넘겨 이어지는 겨울방학의 시작일이 잘리지 않도록 지난해 12월부터,
    // 다음 해 1학기 시작 전 일정까지 담기도록 다음 해 3월까지 조회한다.
    private static final Month SEARCH_START_MONTH = Month.DECEMBER;
    private static final Month SEARCH_END_MONTH = Month.MARCH;

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final HomeTimeRepository homeTimeRepository;
    private final SchoolScheduleReader schoolScheduleReader;
    private final Clock clock;

    // 외부 API를 호출하는 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션으로 묶지 않는다.
    public GetCountdownListResponse execute(Long userId, Role role) {
        School school = resolveSchool(userId, role);

        OffsetDateTime serverNow = OffsetDateTime.now(clock);
        LocalDate today = serverNow.toLocalDate();
        List<ExternalSchedule> schedules = fetchSchedules(school, today);

        return GetCountdownListResponse.builder()
                .serverNow(serverNow)
                .countdowns(List.of(
                        resolveHome(school.getSchoolId(), today),
                        resolveSchedule(CountdownType.VACATION, VACATION_TITLE, schedules, today,
                                this::isVacation, this::vacationTitle),
                        resolveSchedule(CountdownType.EXAM, EXAM_TITLE, schedules, today,
                                this::isExam, this::examTitle)))
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

    private List<ExternalSchedule> fetchSchedules(School school, LocalDate today) {
        LocalDate from = LocalDate.of(today.getYear() - 1, SEARCH_START_MONTH, 1);
        LocalDate to = LocalDate.of(today.getYear() + 1, SEARCH_END_MONTH, SEARCH_END_MONTH.length(false));
        return schoolScheduleReader.read(school.getAtptCode(), school.getSchoolCode(), from, to);
    }

    // 하교 시각이 지나도 당일에는 그대로 내려주고, 날짜가 바뀌면 그날의 하교 시각으로 바뀐다.
    private CountdownSummaryResponse resolveHome(Long schoolId, LocalDate today) {
        LocalTime dismissalTime = homeTimeRepository.findBySchoolSchoolIdAndDate(schoolId, today)
                .map(HomeTime::getDismissalTime)
                .orElse(null);
        return countdown(CountdownType.HOME, HOME_TITLE,
                dismissalTime == null ? null : today.atTime(dismissalTime));
    }

    // 하루 단위로 내려온 일정 중 이름이 같고 날짜가 이어지는 것을 한 기간으로 묶어,
    // 오늘 이후에 시작하는 기간 가운데 가장 빠른 것을 고른다. (이미 시작한 일정은 건너뛴다)
    private CountdownSummaryResponse resolveSchedule(CountdownType type, String defaultTitle,
                                                     List<ExternalSchedule> schedules, LocalDate today,
                                                     Predicate<String> matcher, UnaryOperator<String> titleMapper) {
        List<ExternalSchedule> matched = schedules.stream()
                .filter(schedule -> matcher.test(schedule.title()))
                .distinct()
                .sorted(Comparator.comparing(ExternalSchedule::title).thenComparing(ExternalSchedule::date))
                .toList();

        ExternalSchedule found = null;
        int index = 0;
        while (index < matched.size()) {
            ExternalSchedule first = matched.get(index++);
            LocalDate endDate = first.date();
            while (index < matched.size()
                    && matched.get(index).title().equals(first.title())
                    && matched.get(index).date().equals(endDate.plusDays(1))) {
                endDate = matched.get(index++).date();
            }
            if (!first.date().isBefore(today) && (found == null || first.date().isBefore(found.date()))) {
                found = first;
            }
        }
        return found == null
                ? countdown(type, defaultTitle, null)
                : countdown(type, titleMapper.apply(found.title()), found.date().atStartOfDay());
    }

    // TODO: NEIS 일정명으로 판별한다. 학교마다 표기가 달라 누락, 오탐이 있으면 조건을 넓혀야 한다.
    private boolean isVacation(String title) {
        if (title.contains("방학식")) {
            return false;
        }
        if (title.contains("방학")) {
            return true;
        }
        // 휴업일은 하계, 동계만 방학으로 본다. (재량휴업일, 토요휴업일은 방학이 아니다)
        return title.contains("휴업일") && (title.contains("하계") || title.contains("동계"));
    }

    private boolean isExam(String title) {
        return title.contains(MIDTERM_TITLE) || title.contains(FINAL_TITLE) || title.contains(WRITTEN_TEST_TITLE);
    }

    private String vacationTitle(String title) {
        if (title.contains("하계") || title.contains("여름")) {
            return SUMMER_VACATION_TITLE;
        }
        if (title.contains("동계") || title.contains("겨울")) {
            return WINTER_VACATION_TITLE;
        }
        return VACATION_TITLE;
    }

    private String examTitle(String title) {
        if (title.contains(MIDTERM_TITLE)) {
            return MIDTERM_TITLE;
        }
        if (title.contains(FINAL_TITLE)) {
            return FINAL_TITLE;
        }
        return WRITTEN_TEST_TITLE;
    }

    private CountdownSummaryResponse countdown(CountdownType type, String title, LocalDateTime targetAt) {
        return CountdownSummaryResponse.builder()
                .type(type)
                .title(title)
                .targetAt(targetAt == null ? null : targetAt.atZone(clock.getZone()).toOffsetDateTime())
                .build();
    }
}
