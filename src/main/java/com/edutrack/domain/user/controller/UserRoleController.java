package com.edutrack.domain.user.controller;

import com.edutrack.domain.user.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academies/{academyId}/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PatchMapping("/{userId}/role/teacher")
    public ResponseEntity<String> changeStudentToTeacher(
            @PathVariable Long academyId,
            @PathVariable Long userId,
            Authentication authentication
    ) {

        Long principalId = (Long) authentication.getPrincipal();

        userRoleService.changeStudentToTeacher(principalId, userId);

        return ResponseEntity.ok("강사 역할 변경 완료");
    }
}