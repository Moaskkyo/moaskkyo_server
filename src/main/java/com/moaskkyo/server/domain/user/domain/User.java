package com.moaskkyo.server.domain.user.domain;

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
import java.time.MonthDay;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "`user`", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_phone_number", columnNames = "phone_number"),
        @UniqueConstraint(name = "uk_user_school_student",
                columnNames = {"school_id", "grade", "class_number", "student_number"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public static final int BIRTH_YEAR = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 11)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "class_number", nullable = false)
    private int classNumber;

    @Column(name = "student_number", nullable = false)
    private int studentNumber;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Builder
    private User(School school, String name, String phoneNumber, String password,
                 int grade, int classNumber, int studentNumber, MonthDay birthday) {
        this.school = school;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.grade = grade;
        this.classNumber = classNumber;
        this.studentNumber = studentNumber;
        this.birth = birthday.atYear(BIRTH_YEAR);
    }

    public boolean isBirthday(LocalDate today) {
        MonthDay birthday = MonthDay.from(birth);
        if (birthday.equals(MonthDay.of(2, 29)) && !today.isLeapYear()) {
            return MonthDay.from(today).equals(MonthDay.of(2, 28));
        }
        return birthday.equals(MonthDay.from(today));
    }
}
