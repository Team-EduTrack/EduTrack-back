package com.edutrack.domain.statistics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edutrack.domain.statistics.dto.StudentAnalysisResponse;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import com.edutrack.domain.statistics.dto.StudentLectureAttendanceResponse;
import com.edutrack.domain.statistics.dto.StudentLectureAverageResponse;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.service.StudentAttendanceService;
import com.edutrack.domain.statistics.service.StudentReportService;
import com.edutrack.global.exception.ForbiddenException;

import lombok.RequiredArgsConstructor;

/**
 * 학생 리포트 컨트롤러
 * 
 * 현재 코드베이스의 복수 요일 지원 기능과 호환됩니다.
 * 
 */
@RestController
@RequiredArgsConstructor
public class StudentReportController {

    private final StudentReportService studentReportService;
    private final StudentAttendanceService studentAttendanceService;

    /**
     * 학생 통합 분석 리포트 API
     */
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

    /**
     * 학생 전체 시험 요약 조회 API
     */
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

    /**
     * 학생의 단원별 성취도 조회 (정답률 낮은 순)
     */
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

    /**
     * 학생의 강의별 월간 출석 현황 조회
     * 
     * @param studentId 학생 ID
     * @param lectureId 강의 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param principalId 인증된 사용자 ID
     * @return 학생의 월별 출석 현황 정보
     */
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

    /**
     * 학생의 특정 강의에 대한 모든 시험 및 과제 결과 평균 점수 조회
     */
    @GetMapping("/api/students/{studentId}/lectures/{lectureId}/average-scores")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<StudentLectureAverageResponse> getLectureAverageScores(
            @PathVariable Long studentId,
            @PathVariable Long lectureId,
            @AuthenticationPrincipal Long userId
    ) {
        if (!studentId.equals(userId)) {
            throw new ForbiddenException("본인의 시험 및 과제 결과 평균 점수만 조회할 수 있습니다.");
        }
        StudentLectureAverageResponse response = studentReportService.getLectureAverageScores(studentId, lectureId);
        return ResponseEntity.ok(response);
    }
}
