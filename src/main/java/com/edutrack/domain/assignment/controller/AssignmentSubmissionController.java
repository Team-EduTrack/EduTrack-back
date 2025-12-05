package com.edutrack.domain.assignment.controller;

import com.edutrack.domain.assignment.dto.AssignmentGradeRequest;
import com.edutrack.domain.assignment.dto.AssignmentGradeResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmissionListResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmissionTeacherViewResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmitRequest;
import com.edutrack.domain.assignment.dto.AssignmentSubmitResponse;
import com.edutrack.domain.assignment.dto.PresignedUrlRequest;
import com.edutrack.domain.assignment.dto.PresignedUrlResponse;
import com.edutrack.domain.assignment.service.AssignmentSubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies/{academyId}/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService assignmentSubmissionService;

    // Presigned URL 요청
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @PathVariable Long assignmentId,
            @RequestBody PresignedUrlRequest request) {

        return ResponseEntity.ok(assignmentSubmissionService.createPresignedUrl(assignmentId, request));
    }

    // 과제 제출 (fileKey 저장)
    @PostMapping("/submit")
    public ResponseEntity<AssignmentSubmitResponse> submit(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal Long studentId,
            @RequestBody AssignmentSubmitRequest request) {


        return ResponseEntity.ok(assignmentSubmissionService.submit(assignmentId, studentId, request));
    }

  // 강사 기준 -> 특정 과제 제출 리스트 조회
  @PreAuthorize("hasRole('TEACHER')")
  @GetMapping("")
  public ResponseEntity<List<AssignmentSubmissionListResponse>> getSubmissions(
      @PathVariable Long assignmentId,
      Authentication authentication
  ) {

    // JWT subject (userId)
    Long teacherId = Long.parseLong(authentication.getName());

    return ResponseEntity.ok(assignmentSubmissionService.getSubmissionsForTeacher(assignmentId, teacherId));
  }

    /**
     * 강사용 – 과제 제출 상세 조회
     * GET /api/academies/{academyId}/assignments/{assignmentId}/submissions/{submissionId}
     */
    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentSubmissionTeacherViewResponse> getSubmissionForTeacher(
            @PathVariable Long academyId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal Long teacherId
    ) {
        var response = assignmentSubmissionService.getSubmissionForTeacher(
                academyId, teacherId, assignmentId, submissionId);

        return ResponseEntity.ok(response);
    }

    /**
     * 강사용 – 점수 + 피드백 저장
     */
    @PatchMapping("/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentGradeResponse> gradeSubmission(
            @PathVariable Long academyId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal Long teacherId,
            @Valid @RequestBody AssignmentGradeRequest request
    ) {
        var response = assignmentSubmissionService.gradeSubmission(
                academyId, teacherId, assignmentId, submissionId, request);

        return ResponseEntity.ok(response);
    }
}