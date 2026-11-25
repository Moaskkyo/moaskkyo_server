package com.moaskkyo.server.domain.teacher.domain.repository;

import com.moaskkyo.server.domain.teacher.domain.Teacher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query("select t from Teacher t join fetch t.school where t.teacherId = :teacherId")
    Optional<Teacher> findWithSchoolByTeacherId(@Param("teacherId") Long teacherId);
}
