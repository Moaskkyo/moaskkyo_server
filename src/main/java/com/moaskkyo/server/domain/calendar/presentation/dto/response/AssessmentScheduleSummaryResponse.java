package com.moaskkyo.server.domain.calendar.presentation.dto.response;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AssessmentScheduleSummaryResponse {

    private final Long assessmentId;
    private final String subjectName;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
}
