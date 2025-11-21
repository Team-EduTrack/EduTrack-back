package com.edutrack.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    Optional<User> findByLoginId(String loginId);
}
