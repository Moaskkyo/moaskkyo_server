package com.moaskkyo.server.domain.hometime.domain;

import com.moaskkyo.server.domain.school.domain.School;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "home_time", uniqueConstraints = {
        @UniqueConstraint(name = "uk_home_time_school_date", columnNames = {"school_id", "date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "home_time_id")
    private Long homeTimeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "dismissal_time")
    private LocalTime dismissalTime;

    @Builder
    private HomeTime(School school, LocalDate date, LocalTime dismissalTime) {
        this.school = school;
        this.date = date;
        this.dismissalTime = dismissalTime;
    }
}
