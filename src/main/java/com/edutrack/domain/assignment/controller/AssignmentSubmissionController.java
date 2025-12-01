package com.edutrack.domain.assignment.controller;

import com.edutrack.domain.assignment.dto.AssignmentSubmissionListResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmitRequest;
import com.edutrack.domain.assignment.dto.AssignmentSubmitResponse;
import com.edutrack.domain.assignment.dto.PresignedUrlRequest;
import com.edutrack.domain.assignment.dto.PresignedUrlResponse;
import com.edutrack.domain.assignment.service.AssignmentSubmissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

  private final AssignmentSubmissionService submissionService;

  // Presigned URL 요청
  @PostMapping("/{assignmentId}/presigned-url")
  public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
      @PathVariable Long assignmentId,
      @RequestBody PresignedUrlRequest request) {

    return ResponseEntity.ok(submissionService.createPresignedUrl(assignmentId, request));
  }

  // 과제 제출 (fileKey 저장)
  @PostMapping("/{assignmentId}/submit")
  public ResponseEntity<AssignmentSubmitResponse> submit(
      @PathVariable Long assignmentId,
      @RequestParam Long studentId,
      @RequestBody AssignmentSubmitRequest request) {

    return ResponseEntity.ok(submissionService.submit(assignmentId, studentId, request));
  }

  // 강사 기준 -> 특정 과제 제출 리스트 조회
  @PreAuthorize("hasRole('TEACHER')")
  @GetMapping("/{assignmentId}/submissions")
  public ResponseEntity<List<AssignmentSubmissionListResponse>> getSubmissions(
      @PathVariable Long assignmentId,
      Authentication authentication
  ) {

    // JWT subject (userId)
    Long teacherId = Long.parseLong(authentication.getName());

    return ResponseEntity.ok(submissionService.getSubmissionsForTeacher(assignmentId, teacherId));
  }


}
