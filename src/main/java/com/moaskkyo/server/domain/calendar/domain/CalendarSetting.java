package com.moaskkyo.server.domain.calendar.domain;

import com.moaskkyo.server.domain.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "calendar_setting", uniqueConstraints = {
        @UniqueConstraint(name = "uk_calendar_setting_user", columnNames = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_setting_id")
    private Long calendarSettingId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "show_academic", nullable = false)
    private boolean showAcademic;

    @Column(name = "show_assessment", nullable = false)
    private boolean showAssessment;

    @Column(name = "show_birthday", nullable = false)
    private boolean showBirthday;

    @Builder
    private CalendarSetting(User user) {
        this.user = user;
        this.showAcademic = true;
        this.showAssessment = true;
        this.showBirthday = true;
    }
}
