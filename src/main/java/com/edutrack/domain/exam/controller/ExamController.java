package com.edutrack.domain.exam.controller;

import com.edutrack.domain.exam.dto.*;
import com.edutrack.domain.exam.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/lectures/{lectureId}/exams")
public class ExamController {
    private final ExamService examService;

    //시험생성
    @PostMapping
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ExamCreationResponse> createExam(
            @PathVariable Long lectureId,
            @Valid @RequestBody ExamCreationRequest request,
            Authentication authentication){
        Long principalUserId = (Long) authentication.getPrincipal();
        request.setLectureId(lectureId);

        Long examId = examService.createExam(principalUserId, request);

        ExamCreationResponse response = ExamCreationResponse.builder()
                .examId(examId)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //문제등록

    @PostMapping("/{examId}/mcq")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<List<QuestionIdResponse>> registerQuestions(
            @PathVariable Long examId,
            @Valid @RequestBody List<QuestionRegistrationRequest> requests){

        List<Long> questionIds = examService.registerQuestions(examId, requests);

        List<QuestionIdResponse> responses =questionIds.stream()
                .map(id -> QuestionIdResponse.builder()
                        .questionId(id)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    //시험 상세조회
    @GetMapping("/{examId}")
    @PreAuthorize("hasAnyRole('PRINCIPAL','TEACHER')")
    public ResponseEntity<ExamDetailResponse> getExamDetail(
            @PathVariable Long examId,
            Authentication authentication){
        Long principalUserId = (Long) authentication.getPrincipal();
        ExamDetailResponse response = examService.getExamDetail(examId, principalUserId);

        return ResponseEntity.ok(response);
    }

    // 시험 제출 - StudentExamController로 이동됨 (/api/student/exams/{examId}/submit)
}
