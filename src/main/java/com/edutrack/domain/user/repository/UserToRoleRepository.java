package com.edutrack.domain.user.repository;

import com.edutrack.domain.user.entity.UserToRole;
import com.edutrack.domain.user.entity.UserToRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserToRoleRepository extends JpaRepository<UserToRole, UserToRoleId> {

}
