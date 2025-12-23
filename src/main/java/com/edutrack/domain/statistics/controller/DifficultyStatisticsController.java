package com.edutrack.domain.statistics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrack.domain.statistics.dto.DifficultyStatisticsResponse;
import com.edutrack.domain.statistics.service.ExamDifficultyStatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DifficultyStatisticsController {

    private final ExamDifficultyStatisticsService difficultyStatisticsService;

    /**
     * 학생용: 특정 시험의 난이도별 정답률 통계 조회 (본인)
     */
    @GetMapping("/student/statistics/exams/{examId}/difficulty")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DifficultyStatisticsResponse>> getStudentExamDifficultyStats(
            @PathVariable Long examId,
            @AuthenticationPrincipal Long studentId
    ) {
        List<DifficultyStatisticsResponse> stats = difficultyStatisticsService
                .getDifficultyStatisticsForStudentExam(examId, studentId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 강사용: 특정 시험의 난이도별 정답률 통계 조회 (전체 학생)
     */
    @GetMapping("/statistics/exams/{examId}/difficulty")
    @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
    public ResponseEntity<List<DifficultyStatisticsResponse>> getExamDifficultyStats(
            @PathVariable Long examId
    ) {
        List<DifficultyStatisticsResponse> stats = difficultyStatisticsService
                .getDifficultyStatisticsForExam(examId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 강사용: 강의의 모든 시험 난이도별 정답률 통계 조회
     */
    @GetMapping("/statistics/lectures/{lectureId}/difficulty")
    @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
    public ResponseEntity<List<DifficultyStatisticsResponse>> getLectureDifficultyStats(
            @PathVariable Long lectureId
    ) {
        List<DifficultyStatisticsResponse> stats = difficultyStatisticsService
                .getDifficultyStatisticsForLecture(lectureId);
        return ResponseEntity.ok(stats);
    }
}
