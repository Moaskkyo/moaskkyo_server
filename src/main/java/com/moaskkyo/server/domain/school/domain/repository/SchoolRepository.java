package com.moaskkyo.server.domain.school.domain.repository;

import com.moaskkyo.server.domain.school.domain.School;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School> findByJoinCode(String joinCode);
}
