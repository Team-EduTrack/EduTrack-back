package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.StudentAnalysisResponse;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import com.edutrack.domain.statistics.dto.StudentLectureAttendanceResponse;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.service.StudentAttendanceService;
import com.edutrack.domain.statistics.service.StudentReportService;
import com.edutrack.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequiredArgsConstructor
public class StudentReportController {

    private final StudentReportService studentReportService;
    private final StudentAttendanceService studentAttendanceService;

    //학생 통합 분석 리포트 API
    @GetMapping("/api/analysis/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<StudentAnalysisResponse> getStudentAnalysis(
            @PathVariable Long studentId,
            @AuthenticationPrincipal Long principalId
    ) {
        if (!studentId.equals(principalId)) {
            throw new ForbiddenException("본인의 학습리포트만 조회할수 있습니다.");
        }

        StudentAnalysisResponse response = studentReportService.getStudentAnalysis(studentId);
        return ResponseEntity.ok(response);
    }

    //학생 전체 시험 요약 조회 API
    @GetMapping("/api/students/{studentId}/analysis/exams")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<List<StudentExamSummaryResponse>> getExamSummary(
            @PathVariable Long studentId,
            @AuthenticationPrincipal Long principalId
    ) {
        if (!studentId.equals(principalId)) {
            throw new ForbiddenException("본인의 시험 기록만 조회할수있습니다.");
        }
        List<StudentExamSummaryResponse> response = studentReportService.getExamSummary(studentId);
        return ResponseEntity.ok(response);
    }

    //학생의 단원별 성취도 조회 (정답률 낮은 순)
    @GetMapping("/api/students/{studentId}/analysis/weak-units")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<List<StudentUnitCorrectRateResponse>> getWeakUnits(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal Long principalId
    ) {
        if (!studentId.equals(principalId)) {
            throw new ForbiddenException("본인의 취약 단원 분석만 조회할 수 있습니다.");
        }
        List<StudentUnitCorrectRateResponse> response = studentReportService.getWeakUnits(studentId, limit);
        return ResponseEntity.ok(response);
    }

    //학생의 강의별 월간 출석 현황 조회
    @GetMapping("/api/students/{studentId}/lectures/{lectureId}/attendance/monthly")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<StudentLectureAttendanceResponse> getMonthlyAttendance(
            @PathVariable Long studentId,
            @PathVariable Long lectureId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal Long principalId
    ) {
        if (!studentId.equals(principalId)) {
            throw new ForbiddenException("본인의 출석 현황만 조회할 수 있습니다.");
        }
        StudentLectureAttendanceResponse response = studentAttendanceService
                .getMonthlyAttendance(studentId, lectureId, year, month);
        return ResponseEntity.ok(response);
    }
}
