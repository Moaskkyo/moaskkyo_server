package com.moaskkyo.server.domain.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.MonthDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    private User userWithBirthday(MonthDay birthday) {
        return User.builder()
                .name("홍길동")
                .phoneNumber("01012345678")
                .password("hashed")
                .grade(2)
                .classNumber(3)
                .studentNumber(15)
                .birthday(birthday)
                .build();
    }

    @Test
    @DisplayName("학년, 반, 번호를 각각 저장한다")
    void gradeClassStudentNumber() {
        User user = userWithBirthday(MonthDay.of(3, 15));

        assertThat(user.getGrade()).isEqualTo(2);
        assertThat(user.getClassNumber()).isEqualTo(3);
        assertThat(user.getStudentNumber()).isEqualTo(15);
    }

    @Test
    @DisplayName("생일은 연도 2000으로 저장한다")
    void birthYearIsFixed() {
        User user = userWithBirthday(MonthDay.of(3, 15));

        assertThat(user.getBirth()).isEqualTo(LocalDate.of(2000, 3, 15));
    }

    @Test
    @DisplayName("오늘이 생일이면 연도와 관계없이 true")
    void isBirthday() {
        User user = userWithBirthday(MonthDay.of(3, 15));

        assertThat(user.isBirthday(LocalDate.of(2026, 3, 15))).isTrue();
        assertThat(user.isBirthday(LocalDate.of(2030, 3, 15))).isTrue();
        assertThat(user.isBirthday(LocalDate.of(2026, 3, 14))).isFalse();
        assertThat(user.isBirthday(LocalDate.of(2026, 4, 15))).isFalse();
    }

    @Test
    @DisplayName("2월 29일 생일은 평년에는 2월 28일, 윤년에는 2월 29일에 표시한다")
    void leapDayBirthday() {
        User user = userWithBirthday(MonthDay.of(2, 29));

        assertThat(user.isBirthday(LocalDate.of(2026, 2, 28))).isTrue();
        assertThat(user.isBirthday(LocalDate.of(2026, 3, 1))).isFalse();
        assertThat(user.isBirthday(LocalDate.of(2028, 2, 29))).isTrue();
        assertThat(user.isBirthday(LocalDate.of(2028, 2, 28))).isFalse();
    }
}
