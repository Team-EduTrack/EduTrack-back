package com.edutrack.domain.exam.controller;

import com.edutrack.domain.exam.dto.*;
import com.edutrack.domain.exam.service.ExamGradingService;
import com.edutrack.domain.exam.service.StudentExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학생 시험 컨트롤러
 * - 시험 응시, 답안 제출, 결과 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/exams")
public class StudentExamController {

    private final StudentExamService studentExamService;
    private final ExamGradingService gradingService;

    /**
     * 시험 응시 시작
     * - 시험을 시작하고 문제 목록을 반환
     */
    @PostMapping("/{examId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamStartResponse> startExam(
            @PathVariable Long examId,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        ExamStartResponse response = studentExamService.startExam(examId, studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 답안 저장 (자동/수동 저장)
     * - 제출 전 답안 임시 저장
     */
    @PostMapping("/{examId}/answers")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AnswerSaveResponse> saveAnswers(
            @PathVariable Long examId,
            @Valid @RequestBody AnswerSaveRequest request,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        AnswerSaveResponse response = studentExamService.saveAnswers(examId, studentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 시험 제출
     * - 저장된 답안으로 시험 제출
     */
    @PostMapping("/{examId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamSubmitResponse> submitExam(
            @PathVariable Long examId,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();

        // 시험 제출
        ExamSubmitResponse response = studentExamService.submitExam(examId, studentId);

        // 자동 채점 실행 (기본 채점)
        gradingService.gradeExam(examId, studentId);

        return ResponseEntity.ok(response);
    }

    /**
     * 시험 결과 조회
     * - 채점 완료된 시험의 결과 확인
     */
    @GetMapping("/{examId}/result")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamResultResponse> getExamResult(
            @PathVariable Long examId,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        ExamResultResponse response = studentExamService.getExamResult(examId, studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 시험 기록 목록 조회
     */
    @GetMapping("/records")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamRecordResponse>> getExamRecords(Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        List<ExamRecordResponse> response = studentExamService.getExamRecords(studentId);
        return ResponseEntity.ok(response);
    }
}





