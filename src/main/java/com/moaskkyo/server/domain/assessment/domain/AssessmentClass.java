package com.moaskkyo.server.domain.assessment.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 수행평가의 대상 반. 한 수행평가가 여러 반 대상이면 반마다 한 행씩 둔다.
@Getter
@Entity
@Table(name = "assessment_class", uniqueConstraints = {
        @UniqueConstraint(name = "uk_assessment_class", columnNames = {"assessment_id", "class_number"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssessmentClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_class_id")
    private Long assessmentClassId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "class_number", nullable = false)
    private int classNumber;

    @Builder
    private AssessmentClass(Assessment assessment, int classNumber) {
        this.assessment = assessment;
        this.classNumber = classNumber;
    }
}
