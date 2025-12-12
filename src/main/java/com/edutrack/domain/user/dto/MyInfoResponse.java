package com.edutrack.domain.user.dto;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.user.entity.User;

public record MyInfoResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role,
        AcademyInfo academy
) {

    public static MyInfoResponse from(User user, String role) {
        Academy academy = user.getAcademy();

        AcademyInfo academyInfo = null;
        if (academy != null) {
            academyInfo = new AcademyInfo(
                    academy.getId(),
                    academy.getName(),
                    academy.getCode()
            );
        }

        return new MyInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                role,
                academyInfo
        );
    }

    public record AcademyInfo(
            Long id,
            String name,
            String code
    ) { }
}