package com.edutrack.domain.unit.controller;

import com.edutrack.domain.unit.dto.UnitCreateRequest;
import com.edutrack.domain.unit.dto.UnitResponse;
import com.edutrack.domain.unit.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/units")
public class UnitController {

    private final UnitService unitService;

    /**
     * 단원 추가
     * - 강의 담당 강사 또는 해당 학원 원장만 추가 가능
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
    public ResponseEntity<UnitResponse> createUnit(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UnitCreateRequest request
    ) {
        UnitResponse response = unitService.createUnit(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 강의별 단원 목록 조회
     */
    @GetMapping("/lectures/{lectureId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL', 'STUDENT')")
    public ResponseEntity<List<UnitResponse>> getUnitsByLectureId(
            @PathVariable Long lectureId
    ) {
        List<UnitResponse> response = unitService.getUnitsByLectureId(lectureId);
        return ResponseEntity.ok(response);
    }
}

