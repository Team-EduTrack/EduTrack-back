package com.edutrack.domain.user.dto;

public record SignInResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) { }