package com.moaskkyo.server.domain.vacation.domain.repository;

import com.moaskkyo.server.domain.vacation.domain.Vacation;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacationRepository extends JpaRepository<Vacation, Long> {

    Optional<Vacation> findFirstBySchoolSchoolIdAndStartDateGreaterThanEqualOrderByStartDateAsc(
            Long schoolId, LocalDate today);
}
