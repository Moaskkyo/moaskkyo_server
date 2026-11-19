package com.moaskkyo.server.domain.calendar.service;

import com.moaskkyo.server.domain.calendar.domain.Academic;
import com.moaskkyo.server.domain.calendar.domain.ScheduleType;
import com.moaskkyo.server.domain.calendar.domain.repository.AcademicRepository;
import com.moaskkyo.server.domain.calendar.domain.repository.CalendarSettingRepository;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.AcademicScheduleSummaryResponse;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.GetAcademicScheduleListResponse;
import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.client.holiday.HolidayClient;
import com.moaskkyo.server.global.client.neis.NeisScheduleClient;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAcademicScheduleListService {

    private static final int MAX_RANGE_DAYS = 42;
    // 조회 범위 경계에 걸친 연속 일정의 시작일이 잘리지 않도록 외부 API는 앞뒤로 넓혀 조회한다.
    private static final int MERGE_PADDING_DAYS = 14;
    private static final Set<String> EXCLUDED_NEIS_TITLES = Set.of("토요휴업일");
    private static final DateTimeFormatter NEIS_KEY_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final UserRepository userRepository;
    private final CalendarSettingRepository calendarSettingRepository;
    private final AcademicRepository academicRepository;
    private final HolidayClient holidayClient;
    private final NeisScheduleClient neisScheduleClient;

    // 외부 API를 호출하는 동안 DB 커넥션을 잡고 있지 않도록 트랜잭션으로 묶지 않는다.
    public GetAcademicScheduleListResponse execute(Long userId, Role role, LocalDate startDate, LocalDate endDate) {
        // TODO: 선생님 조회는 선생님 계정이 생기면 연결한다. (캘린더 설정 없이 항상 on)
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
                .isShowAcademic();

        LocalDate searchStartDate = startDate.minusDays(MERGE_PADDING_DAYS);
        LocalDate searchEndDate = endDate.plusDays(MERGE_PADDING_DAYS);
        List<ExternalSchedule> holidays = holidayClient.getHolidays(searchStartDate, searchEndDate);
        List<AcademicScheduleSummaryResponse> schedules = new ArrayList<>(merge(ScheduleType.HOLIDAY, holidays));

        if (enabled) {
            School school = user.getSchool();
            List<ExternalSchedule> neisSchedules = neisScheduleClient
                    .getSchedules(school.getAtptCode(), school.getSchoolCode(), searchStartDate, searchEndDate)
                    .stream()
                    .filter(schedule -> !EXCLUDED_NEIS_TITLES.contains(schedule.title()))
                    .filter(schedule -> !holidays.contains(schedule))
                    .toList();
            schedules.addAll(merge(ScheduleType.NEIS, neisSchedules));

            for (Academic academic : academicRepository.findAllBySchoolAndPeriod(school, startDate, endDate)) {
                schedules.add(AcademicScheduleSummaryResponse.builder()
                        .type(ScheduleType.REGISTERED)
                        .academicId(academic.getAcademicId())
                        .title(academic.getTitle())
                        .startDate(academic.getStartDate())
                        .endDate(academic.getEndDate())
                        .build());
            }
        }

        return GetAcademicScheduleListResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .enabled(enabled)
                .schedules(schedules.stream()
                        .filter(schedule -> !schedule.getStartDate().isAfter(endDate)
                                && !schedule.getEndDate().isBefore(startDate))
                        .sorted(Comparator.comparing(AcademicScheduleSummaryResponse::getStartDate)
                                .thenComparing(AcademicScheduleSummaryResponse::getType))
                        .toList())
                .build();
    }

    // 하루 단위로 내려온 일정 중 이름이 같고 날짜가 끊기지 않고 이어지는 것을 하나의 기간 일정으로 묶는다.
    private List<AcademicScheduleSummaryResponse> merge(ScheduleType type, List<ExternalSchedule> dailySchedules) {
        List<ExternalSchedule> sorted = dailySchedules.stream()
                .distinct()
                .sorted(Comparator.comparing(ExternalSchedule::title).thenComparing(ExternalSchedule::date))
                .toList();

        List<AcademicScheduleSummaryResponse> merged = new ArrayList<>();
        int index = 0;
        while (index < sorted.size()) {
            ExternalSchedule first = sorted.get(index++);
            LocalDate endDate = first.date();
            while (index < sorted.size()
                    && sorted.get(index).title().equals(first.title())
                    && sorted.get(index).date().equals(endDate.plusDays(1))) {
                endDate = sorted.get(index++).date();
            }
            merged.add(AcademicScheduleSummaryResponse.builder()
                    .type(type)
                    .neisKey(type == ScheduleType.NEIS
                            ? first.date().format(NEIS_KEY_DATE_FORMAT) + "_" + first.title()
                            : null)
                    .title(first.title())
                    .startDate(first.date())
                    .endDate(endDate)
                    .build());
        }
        return merged;
    }
}
