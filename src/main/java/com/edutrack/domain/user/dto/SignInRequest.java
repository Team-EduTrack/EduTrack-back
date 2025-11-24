package com.edutrack.domain.user.dto;

public record SignInRequest(
        String loginId,
        String password
) {
}
