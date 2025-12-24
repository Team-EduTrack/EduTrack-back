package com.edutrack.domain.principal.controller;

import com.edutrack.domain.principal.dto.PrincipalLectureResponse;
import com.edutrack.domain.principal.service.PrincipalLectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/principal")
public class PrincipalLectureController {

    private final PrincipalLectureService principalLectureService;

    /**
     * 원장의 학원 내 전체 강의 조회 (페이지네이션)
     * - 10개씩 페이징
     * - ID 내림차순 정렬 (최신순)
     */
    @GetMapping("/lectures")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<Page<PrincipalLectureResponse>> getLectures(
            @AuthenticationPrincipal Long principalId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PrincipalLectureResponse> response = principalLectureService.getLecturesByAcademy(principalId, pageable);
        return ResponseEntity.ok(response);
    }
}

