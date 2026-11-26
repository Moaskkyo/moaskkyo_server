package com.moaskkyo.server.domain.calendar.service;

import com.moaskkyo.server.domain.calendar.domain.CalendarSetting;
import com.moaskkyo.server.domain.calendar.domain.repository.CalendarSettingRepository;
import com.moaskkyo.server.domain.calendar.presentation.dto.request.UpdateCalendarSettingRequest;
import com.moaskkyo.server.domain.calendar.presentation.dto.response.UpdateCalendarSettingResponse;
import com.moaskkyo.server.domain.user.domain.User;
import com.moaskkyo.server.domain.user.domain.repository.UserRepository;
import com.moaskkyo.server.global.auth.Role;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCalendarSettingService {

    private final UserRepository userRepository;
    private final CalendarSettingRepository calendarSettingRepository;

    @Transactional
    public UpdateCalendarSettingResponse execute(Long userId, Role role, UpdateCalendarSettingRequest request) {
        // 선생님은 캘린더 설정이 없다.
        if (role != Role.STUDENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (request.getShowAcademic() == null
                && request.getShowAssessment() == null
                && request.getShowBirthday() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        CalendarSetting calendarSetting = calendarSettingRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.CALENDAR_SETTING_NOT_FOUND));

        calendarSetting.update(request.getShowAcademic(), request.getShowAssessment(), request.getShowBirthday());

        return UpdateCalendarSettingResponse.builder()
                .showAcademic(calendarSetting.isShowAcademic())
                .showAssessment(calendarSetting.isShowAssessment())
                .showBirthday(calendarSetting.isShowBirthday())
                .build();
    }
}
