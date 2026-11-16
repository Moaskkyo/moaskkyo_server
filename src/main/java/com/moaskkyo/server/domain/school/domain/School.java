package com.moaskkyo.server.domain.school.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "school", uniqueConstraints = {
        @UniqueConstraint(name = "uk_school_join_code", columnNames = "join_code")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School {

    public static final int JOIN_CODE_LENGTH = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "atpt_code", nullable = false)
    private String atptCode;

    @Column(name = "school_code", nullable = false)
    private String schoolCode;

    @Column(name = "join_code", nullable = false, length = JOIN_CODE_LENGTH)
    private String joinCode;

    @Builder
    private School(String name, String atptCode, String schoolCode, String joinCode) {
        this.name = name;
        this.atptCode = atptCode;
        this.schoolCode = schoolCode;
        this.joinCode = joinCode;
    }
}
