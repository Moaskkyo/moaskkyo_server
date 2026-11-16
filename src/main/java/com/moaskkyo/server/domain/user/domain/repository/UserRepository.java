package com.moaskkyo.server.domain.user.domain.repository;

import com.moaskkyo.server.domain.school.domain.School;
import com.moaskkyo.server.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsBySchoolAndGradeAndClassNumberAndStudentNumber(
            School school, int grade, int classNumber, int studentNumber);

    @Query("select u from User u join fetch u.school where u.phoneNumber = :phoneNumber")
    Optional<User> findWithSchoolByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
