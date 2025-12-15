package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.dto.UnitCorrectRateResponse;
import com.edutrack.domain.statistics.service.UnitStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics/units")
@RequiredArgsConstructor
public class UnitStatisticsController {

    private final UnitStatisticsService service;

    // 전체 학생 기준 단원 정답률
    @GetMapping("/{unitId}/correct-rate")
    public UnitCorrectRateResponse getUnitCorrectRate(@PathVariable Long unitId) {
        return service.getUnitCorrectRate(unitId);
    }

    // 특정 학생 단원 정답률
    @GetMapping("/{unitId}/students/{studentId}/correct-rate")
    public StudentUnitCorrectRateResponse getStudentUnitCorrectRate(
            @PathVariable Long unitId,
            @PathVariable Long studentId
    ) {
        return service.getStudentUnitCorrectRate(studentId, unitId);
    }
}
