package com.edutrack.domain.exam.controller;

import com.edutrack.domain.exam.dto.ExamStartResponse;
import com.edutrack.domain.exam.service.ExamStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 학생 시험 응시 플로우(응시 시작 / 제출 / 결과 조회) 중
 * "응시 시작" API를 담당하는 컨트롤러
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams/{examId}/student")
public class ExamStudentController {

    private final ExamStudentService examStudentService;

    // 시험 응시 시작
    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamStartResponse> startExam(
            @PathVariable Long examId,
            Authentication authentication)
    {
        Long studentId = (Long) authentication.getPrincipal();

        ExamStartResponse response = examStudentService.startExam(examId, studentId);
        log.info("시험 응시 시작 API호출 - examId={}, studentId={}", examId, studentId);

        return ResponseEntity.ok(response);

    }
}
