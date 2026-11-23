package com.moaskkyo.server.domain.countdown.service;

import com.moaskkyo.server.domain.countdown.domain.CountdownType;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.CountdownSummaryResponse;
import com.moaskkyo.server.domain.countdown.presentation.dto.response.GetCountdownListResponse;
import com.moaskkyo.server.domain.hometime.domain.HomeTime;
import com.moaskkyo.server.domain.hometime.domain.repository.HomeTimeRepository;
import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.client.neis.NeisScheduleClient;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCountdownListService {

    private static final String HOME_TITLE = "하교";
    // TODO: 일정이 없을 때 쓰는 기본 제목. 화면에 보일 문구라 기획 확인이 필요하다.
    private static final String VACATION_TITLE = "방학";
    private static final String EXAM_TITLE = "시험";

    // 이미 시작한 방학의 시작일이 잘리지 않도록 과거까지 넓혀 조회한다.
    private static final int SEARCH_BACK_DAYS = 90;
    private static final int SEARCH_FORWARD_MONTHS = 6;

    private final UserRepository userRepository;
    private final HomeTimeRepository homeTimeRepository;
    private final NeisScheduleClient neisScheduleClient;
    private final Clock clock;

    // 외부 API를 호출하는 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션으로 묶지 않는다.
    public GetCountdownListResponse execute(Long userId, Role role) {
        // TODO: 선생님 조회는 선생님 계정이 생기면 연결한다.
        if (role != Role.STUDENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findWithSchoolByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        OffsetDateTime serverNow = OffsetDateTime.now(clock);
        LocalDate today = serverNow.toLocalDate();
        List<ExternalSchedule> schedules = fetchSchedules(user.getSchool(), today);

        return GetCountdownListResponse.builder()
                .serverNow(serverNow)
                .countdowns(List.of(
                        resolveHome(user.getSchool().getSchoolId(), today),
                        resolveSchedule(CountdownType.VACATION, VACATION_TITLE, schedules, today, this::isVacation),
                        resolveSchedule(CountdownType.EXAM, EXAM_TITLE, schedules, today, this::isExam)))
                .build();
    }

    // NEIS가 응답하지 않아도 하교 남은 시간은 내려갈 수 있도록 빈 목록으로 넘긴다.
    private List<ExternalSchedule> fetchSchedules(School school, LocalDate today) {
        try {
            return neisScheduleClient.getSchedules(school.getAtptCode(), school.getSchoolCode(),
                    today.minusDays(SEARCH_BACK_DAYS), today.plusMonths(SEARCH_FORWARD_MONTHS));
        } catch (BusinessException e) {
            log.warn("NEIS 학사일정을 불러오지 못해 방학, 시험 일정을 비웁니다.", e);
            return List.of();
        }
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
    // 아직 끝나지 않은 기간 가운데 가장 빨리 시작한 것을 고른다. (기간 중이면 그 일정을 계속 보여준다)
    private CountdownSummaryResponse resolveSchedule(CountdownType type, String defaultTitle,
                                                     List<ExternalSchedule> schedules, LocalDate today,
                                                     Predicate<String> matcher) {
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
            if (!endDate.isBefore(today) && (found == null || first.date().isBefore(found.date()))) {
                found = first;
            }
        }
        return found == null
                ? countdown(type, defaultTitle, null)
                : countdown(type, found.title(), found.date().atStartOfDay());
    }

    // TODO: NEIS 일정명으로 판별한다. 학교마다 표기가 달라 누락, 오탐이 있으면 조건을 넓혀야 한다.
    private boolean isVacation(String title) {
        return title.contains("방학") && !title.contains("방학식");
    }

    private boolean isExam(String title) {
        return (title.contains("고사") || title.contains("지필평가")) && !title.contains("모의고사");
    }

    private CountdownSummaryResponse countdown(CountdownType type, String title, LocalDateTime targetAt) {
        return CountdownSummaryResponse.builder()
                .type(type)
                .title(title)
                .targetAt(targetAt == null ? null : targetAt.atZone(clock.getZone()).toOffsetDateTime())
                .build();
    }
}
