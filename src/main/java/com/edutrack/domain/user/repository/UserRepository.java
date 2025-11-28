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
      join u.userToRoles ur
      join ur.role r
      where u.academy.id = :academyId
        and r.name = :roleType
        and (
              u.name like %:keyword%
           or u.loginId like %:keyword%
           or u.phone like %:keyword%
           or u.email like %:keyword%
        )
      """)
  List<User> searchByAcademyAndRoleAndKeyword(
      @Param("academyId") Long academyId,
      @Param("roleType") RoleType roleType,
      @Param("keyword") String keyword
  );

  //강의 배정을 위한 해당 학원 소속의 강의에 배정되지 않은 학생 검색
  @Query("""
  SELECT DISTINCT u
  FROM User u
  JOIN u.userToRoles ur
  JOIN ur.role r
  WHERE u.academy.id = :academyId
    AND r.name = com.edutrack.domain.user.entity.RoleType.STUDENT
    AND (:name IS NULL OR u.name LIKE %:name%)
    AND u.id NOT IN :excludedIds
""")
  List<User> findAvailableStudents(
      @Param("academyId") Long academyId,
      @Param("excludedIds") List<Long> excludedIds,
      @Param("name") String name
  );

}