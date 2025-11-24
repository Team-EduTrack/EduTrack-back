package com.edutrack.domain.user.util;

import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;

import java.util.Comparator;

public final class RoleUtils {

    private RoleUtils() {
        // 유틸 클래스라 인스턴스 생성 막기
    }

    /**
     * 대표 Role 1개 (ADMIN > PRINCIPAL > TEACHER > STUDENT)
     */
    public static String extractPrimaryRoleName(User user) {
        return user.getRoles().stream()
                .map(Role::getName)                           // RoleType
                .min(Comparator.comparingInt(RoleUtils::priority))
                .map(Enum::name)                              // "TEACHER"
                .orElse("STUDENT");
    }

    private static int priority(RoleType roleType) {
        return switch (roleType) {
            case ADMIN -> 1;
            case PRINCIPAL -> 2;
            case TEACHER -> 3;
            case STUDENT -> 4;
        };
    }
}