package com.edutrack.domain.assignment.controller;

import com.edutrack.domain.assignment.dto.AssignmentCreateRequest;
import com.edutrack.domain.assignment.dto.AssignmentCreateResponse;
import com.edutrack.domain.assignment.dto.AssignmentListResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmissionStudentViewResponse;
import com.edutrack.domain.assignment.service.AssignmentService;
import com.edutrack.domain.assignment.service.AssignmentSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academies/{academyId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService assignmentSubmissionService;
    /**
     * 과제 생성 API
     * POST /api/academies/1/assignments
     */
    @PostMapping
    public ResponseEntity<AssignmentCreateResponse> createAssignment(
            @PathVariable Long academyId,
            @AuthenticationPrincipal Long teacherId,
            @RequestBody AssignmentCreateRequest request
    ) {
        AssignmentCreateResponse response =
                assignmentService.createAssignment(academyId, teacherId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 학생용 – 특정 강의의 과제 리스트 조회
     * GET /api/academies/{academyId}/assignments/lectures/{lectureId}
     */
    @GetMapping("/lectures/{lectureId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AssignmentListResponse>> getAssignmentsForLecture(
            @PathVariable Long academyId,
            @PathVariable Long lectureId,
            @AuthenticationPrincipal Long studentId
    ) {
        List<AssignmentListResponse> responses =
                assignmentService.getAssignmentsForLecture(academyId, studentId, lectureId);

        return ResponseEntity.ok(responses);
    }

    /**
     * 학생용 – 자신의 과제 제출 상세 조회 (점수/피드백 읽기 전용)
     */
    @GetMapping("/{assignmentId}/my-submission")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentSubmissionStudentViewResponse> getMySubmission(
            @PathVariable Long academyId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal Long studentId
    ) {
        var response = assignmentSubmissionService.getMySubmission(
                academyId, studentId, assignmentId);

        return ResponseEntity.ok(response);
    }
}