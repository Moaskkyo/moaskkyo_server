package com.moaskkyo.server.domain.calendar.domain.repository;

import com.moaskkyo.server.domain.calendar.domain.CalendarSetting;
import com.moaskkyo.server.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarSettingRepository extends JpaRepository<CalendarSetting, Long> {

    Optional<CalendarSetting> findByUser(User user);
}
