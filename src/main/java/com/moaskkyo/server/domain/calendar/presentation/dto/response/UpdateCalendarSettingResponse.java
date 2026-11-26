package com.moaskkyo.server.domain.calendar.presentation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UpdateCalendarSettingResponse {

    private final boolean showAcademic;
    private final boolean showAssessment;
    private final boolean showBirthday;
}
