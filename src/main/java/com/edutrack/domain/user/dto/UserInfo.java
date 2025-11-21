package com.edutrack.domain.user.dto;

public record UserInfo(
        Long id,
        String name,
        String role
) { }