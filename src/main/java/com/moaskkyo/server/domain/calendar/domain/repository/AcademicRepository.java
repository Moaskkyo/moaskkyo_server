package com.moaskkyo.server.domain.calendar.domain.repository;

import com.moaskkyo.server.domain.calendar.domain.Academic;
import com.moaskkyo.server.domain.school.domain.School;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicRepository extends JpaRepository<Academic, Long> {

    @Query("select a from Academic a "
            + "where a.school = :school and a.startDate <= :endDate and a.endDate >= :startDate")
    List<Academic> findAllBySchoolAndPeriod(@Param("school") School school,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
