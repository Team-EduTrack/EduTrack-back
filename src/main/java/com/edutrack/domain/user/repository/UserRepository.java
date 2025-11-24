package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByLoginId(String loginId);

    @Query("""
        select distinct u
        from User u
        join u.roles r
        where u.academy.id = :academyId
          and (:roleType is null or r.name = :roleType)
          and (:keyword is null 
               or u.loginId like concat('%', :keyword, '%')
               or u.phone like concat('%', :keyword, '%'))
        """)
    List<User> searchByAcademyAndRoleAndKeyword(Long academyId, RoleType roleType, String keyword);
}