package com.edutrack.domain.user.service;

import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void changeStudentToTeacher(Long principalId, Long targetUserId) {

        //요청한 사람 조회
        User principal = userRepository.findById(principalId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 사용자를 찾을 수 없습니다."));

        //원장 권한인지 검사
        boolean isPrincipal = principal.hasRole("PRINCIPAL");
        if (!isPrincipal) {
            throw new AccessDeniedException("원장만 학생을 강사로 변경할 수 있습니다.");
        }

        //변경 대상 학생 조회
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        Role teacherRole = roleRepository.findByName("TEACHER")
                .orElseThrow(() -> new IllegalArgumentException("TEACHER 역할이 존재하지 않습니다."));

        boolean alreadyTeacher = target.hasRole("TEACHER");
        if (alreadyTeacher) {
            return;
        }

        target.addRole(teacherRole);
    }
}