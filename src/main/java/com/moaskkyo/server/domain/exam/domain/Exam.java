package com.moaskkyo.server.domain.exam.domain;

import com.moaskkyo.server.domain.school.domain.School;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "exam_table")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long examId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // TODO: Subject 엔티티가 추가되면 @ManyToOne 연관관계와 외래키로 바꾼다.
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ExamType type;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "period", nullable = false)
    private int period;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Column(name = "major")
    private String major;

    @Builder
    private Exam(School school, Long subjectId, ExamType type, LocalDate date,
                 int period, int grade, String major) {
        this.school = school;
        this.subjectId = subjectId;
        this.type = type;
        this.date = date;
        this.period = period;
        this.grade = grade;
        this.major = major;
    }
}
