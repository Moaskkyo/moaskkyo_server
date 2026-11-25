package com.moaskkyo.server.domain.assessment.domain.repository;

import com.moaskkyo.server.domain.assessment.domain.Assessment;
import com.moaskkyo.server.domain.school.domain.School;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("select a from Assessment a join fetch a.subject "
            + "where a.school = :school and a.schoolYear = :schoolYear and a.grade = :grade "
            + "and a.startDate <= :endDate and a.endDate >= :startDate "
            + "and exists (select 1 from AssessmentClass ac "
            + "where ac.assessment = a and ac.classNumber = :classNumber) "
            + "order by a.startDate, a.assessmentId")
    List<Assessment> findAllByTargetClassAndPeriod(@Param("school") School school,
                                                   @Param("schoolYear") int schoolYear,
                                                   @Param("grade") int grade,
                                                   @Param("classNumber") int classNumber,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}
