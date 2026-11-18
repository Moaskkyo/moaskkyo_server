package com.moaskkyo.server.domain.hometime.domain.repository;

import com.moaskkyo.server.domain.hometime.domain.HomeTime;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeTimeRepository extends JpaRepository<HomeTime, Long> {

    Optional<HomeTime> findBySchoolSchoolIdAndDate(Long schoolId, LocalDate date);
}
