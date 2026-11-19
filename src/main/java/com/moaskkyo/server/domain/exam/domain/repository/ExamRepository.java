package com.moaskkyo.server.domain.exam.domain.repository;

import com.moaskkyo.server.domain.exam.domain.Exam;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findFirstBySchoolSchoolIdAndDateGreaterThanEqualOrderByDateAscPeriodAsc(
            Long schoolId, LocalDate today);
}
