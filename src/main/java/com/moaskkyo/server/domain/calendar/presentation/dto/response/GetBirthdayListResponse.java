package com.moaskkyo.server.domain.calendar.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetBirthdayListResponse {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean enabled;
    private final List<BirthdaySummaryResponse> birthdays;
}
