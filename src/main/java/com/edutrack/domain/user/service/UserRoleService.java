package com.edutrack.domain.user.service;

import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
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

    /**
     * 원장(principalId)이 특정 유저(userId)를 강사(TEACHER)로 변경/추가
     */
    @Transactional
    public void changeStudentToTeacher(Long principalId, Long userId) {
        // 1. 원장 조회
        User principal = userRepository.findById(principalId)
                .orElseThrow(() -> new IllegalArgumentException("원장을 찾을 수 없습니다. id=" + principalId));

        // 2. 권한 체크 (ADMIN 또는 PRINCIPAL만 허용)
        if (!principal.hasRole(RoleType.ADMIN) && !principal.hasRole(RoleType.PRINCIPAL)) {
            throw new IllegalStateException("강사 역할 변경 권한이 없습니다.");
        }

        //대상 유저 조회
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다. id=" + userId));

        //같은 학원인지 확인
        if (!principal.getAcademy().getId().equals(target.getAcademy().getId())) {
            throw new IllegalStateException("다른 학원 소속 사용자는 변경할 수 없습니다.");
        }

        //TEACHER Role 조회
        Role teacherRole = roleRepository.findByName(RoleType.TEACHER)
                .orElseThrow(() -> new IllegalArgumentException("TEACHER 역할이 존재하지 않습니다."));

        //이미 TEACHER면 아무 것도 안 함(나중에 예외 만들예정)
        if (target.hasRole(RoleType.TEACHER)) {
            return;
        }

        // 7. 역할 추가
        target.addRole(teacherRole);
    }
}