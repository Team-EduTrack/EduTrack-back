package com.edutrack.domain.user.entity;

public enum UserStatus {
    PENDING,    // 이메일 인증 전
    ACTIVE,     // 정상 계정 (인증 완료)
    INACTIVE    // 비활성 상태
}