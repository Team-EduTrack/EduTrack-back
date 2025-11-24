package com.edutrack.domain.user.service;


import com.edutrack.domain.user.dto.UserSearchResultResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static com.edutrack.domain.user.util.RoleUtils.extractPrimaryRoleName;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 특정 유저 상세 조회 (원장 화면에서 "이 유저 정보 보기" 용도)
     */
    @Transactional(readOnly = true)
    public UserSearchResultResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        String roleName = extractPrimaryRoleName(user); // 대표 Role 1개

        return new UserSearchResultResponse(
                user.getId(),
                user.getName(),
                user.getLoginId(),
                user.getPhone(),
                roleName
        );
    }

    /**
     * 학원 내 유저 검색
     *
     * - academyId : 해당 학원에 속한 사용자만
     * - roleType  : STUDENT/TEACHER/PRINCIPAL/ADMIN (null 이면 모든 역할)
     * - keyword   : loginId 또는 phone 에 포함되는 문자열 (null 이면 전체)
     */
    @Transactional(readOnly = true)
    public List<UserSearchResultResponse> searchUsers(Long academyId, RoleType roleType, String keyword) {
        List<User> users = userRepository.searchByAcademyAndRoleAndKeyword(
                academyId,
                roleType,
                keyword
        );

        return users.stream()
                .map(user -> new UserSearchResultResponse(
                        user.getId(),
                        user.getName(),
                        user.getLoginId(),
                        user.getPhone(),
                        extractPrimaryRoleName(user)  // 대표 Role 1개
                ))
                .toList();
    }
}