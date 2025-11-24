package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(RoleType name);

}
