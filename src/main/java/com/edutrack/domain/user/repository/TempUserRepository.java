package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.TempUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempUserRepository extends JpaRepository<TempUser, Long> {

  boolean existsByEmail(String email);

  boolean existsByLoginId(String loginId);

  boolean existsByPhone(String phone);

  Optional<TempUser> findByEmail(String email);

}
