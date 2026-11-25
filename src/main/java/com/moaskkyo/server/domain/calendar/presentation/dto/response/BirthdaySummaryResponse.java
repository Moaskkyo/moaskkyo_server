package com.moaskkyo.server.domain.calendar.presentation.dto.response;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BirthdaySummaryResponse {

    private final Long userId;
    private final String name;
    private final int grade;
    private final int classNumber;
    private final int studentNumber;
    private final LocalDate date;
}
