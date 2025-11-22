package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    @Query("""
                SELECT u
                FROM User u
                WHERE u.academyId = :academyId
                  AND (u.loginId LIKE %:keyword%
                       OR u.phone LIKE %:keyword%)
            """)
    List<User> searchByLoginIdOrPhone(
            @Param("academyId") Long academyId,
            @Param("keyword") String keyword
    );
}