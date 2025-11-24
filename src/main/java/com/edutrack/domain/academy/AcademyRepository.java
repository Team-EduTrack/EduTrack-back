package com.edutrack.domain.academy;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRepository extends JpaRepository<Academy, Long> {

  Optional<Academy> findByCode(String code);

}
