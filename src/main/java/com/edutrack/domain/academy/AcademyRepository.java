package com.edutrack.domain.academy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRepository extends JpaRepository<Academy, Long> {
    boolean existsByCode(String code);
}
