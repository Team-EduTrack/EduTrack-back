package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.StudentAnalysisResponse;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.service.StudentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentReportController {

    private final StudentReportService studentReportService;

    //학생 통합 분석 리포트 API
    @GetMapping("/api/analysis/student/{id}")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<StudentAnalysisResponse> getStudentAnalysis(
            @PathVariable Long id,
            Authentication authentication
    ) {
        StudentAnalysisResponse response = studentReportService.getStudentAnalysis(id);
        return ResponseEntity.ok(response);
    }

    //학생 전체 시험 요약 조회 API
    @GetMapping("/api/students/{studentId}/analysis/exams")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<List<StudentExamSummaryResponse>> getExamSummary(
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        List<StudentExamSummaryResponse> response = studentReportService.getExamSummary(studentId);
        return ResponseEntity.ok(response);
    }

    //학생의 단원별 성취도 조회 (정답률 낮은 순)
    @GetMapping("/api/students/{studentId}/analysis/weak-units")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<List<StudentUnitCorrectRateResponse>> getWeakUnits(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication
    ) {
        List<StudentUnitCorrectRateResponse> response = studentReportService.getWeakUnits(studentId, limit);
        return ResponseEntity.ok(response);
    }
}
