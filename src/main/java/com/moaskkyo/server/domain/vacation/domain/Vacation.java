package com.moaskkyo.server.domain.vacation.domain;

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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "vacation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vacation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_id")
    private Long vacationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Builder
    private Vacation(School school, String title, LocalDate startDate, LocalDate endDate) {
        this.school = school;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
