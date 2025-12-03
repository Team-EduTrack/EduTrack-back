package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.UserEmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEmailVerificationRepository extends JpaRepository<UserEmailVerification, Long> {

  // 최신 코드 가져오기
  Optional<UserEmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

}
