package com.moaskkyo.server.domain.calendar.presentation.dto.response;

import com.moaskkyo.server.domain.calendar.domain.ScheduleType;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AcademicScheduleSummaryResponse {

    private final ScheduleType type;
    private final Long academicId;
    private final String neisKey;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
}
