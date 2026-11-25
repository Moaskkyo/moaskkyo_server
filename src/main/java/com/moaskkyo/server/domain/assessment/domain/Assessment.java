package com.moaskkyo.server.domain.assessment.domain;

import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.subject.domain.Subject;
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
@Table(name = "assessment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long assessmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "school_year", nullable = false)
    private int schoolYear;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Builder
    private Assessment(School school, Subject subject, int schoolYear, int grade,
                       String title, LocalDate startDate, LocalDate endDate) {
        this.school = school;
        this.subject = subject;
        this.schoolYear = schoolYear;
        this.grade = grade;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
