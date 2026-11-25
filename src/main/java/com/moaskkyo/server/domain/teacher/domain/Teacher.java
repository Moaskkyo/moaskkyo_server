package com.moaskkyo.server.domain.teacher.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "teacher", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teacher_phone_number", columnNames = "phone_number")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher {

    // TODO: Place, Subject 엔티티가 추가되면 ERD대로 place_id, subject_id 연관관계를 넣는다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long teacherId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 11)
    private String phoneNumber;

    @Builder
    private Teacher(School school, String name, String password, String phoneNumber) {
        this.school = school;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
}
