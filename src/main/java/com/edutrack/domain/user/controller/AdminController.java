package com.edutrack.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrack.domain.user.dto.SearchAllUserResponse;
import com.edutrack.domain.user.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 전용 모든 사용자 조회 API
     * GET /api/admin/users
     * 권한: ADMIN만 접근 가능
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SearchAllUserResponse> getAllUsers() {
        SearchAllUserResponse response = adminService.getAllUsers();
        return ResponseEntity.ok(response);
    }
}
