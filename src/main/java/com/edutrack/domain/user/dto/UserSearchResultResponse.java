package com.edutrack.domain.user.dto;

public record UserSearchResultResponse(
        Long id,
        String name,
        String loginId,
        String phone,
        String role
) { }