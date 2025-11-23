package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByLoginId(String loginId);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

}
