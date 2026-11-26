package com.moaskkyo.server.domain.admin.domain.repository;

import com.moaskkyo.server.domain.admin.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("select a from Admin a join fetch a.school where a.phoneNumber = :phoneNumber")
    Optional<Admin> findWithSchoolByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
