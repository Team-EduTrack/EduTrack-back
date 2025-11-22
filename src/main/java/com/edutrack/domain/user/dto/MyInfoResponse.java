package com.edutrack.domain.user.dto;

public record MyInfoResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role
) { }