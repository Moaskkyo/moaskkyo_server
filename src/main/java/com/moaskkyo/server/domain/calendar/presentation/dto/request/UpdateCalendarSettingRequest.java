package com.moaskkyo.server.domain.calendar.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 보내지 않은 항목(null)은 기존 값을 유지해야 하므로 boolean이 아니라 Boolean으로 받는다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateCalendarSettingRequest {

    private Boolean showAcademic;
    private Boolean showAssessment;
    private Boolean showBirthday;
}
