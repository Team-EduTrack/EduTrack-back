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
@RequestMapping("/api/academies/{academyId}/lectures/{lectureId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService assignmentSubmissionService;
    /**
     * 과제 생성 API
     * POST /api/academies/{academyId}/lectures/{lectureId}/assignments
     */
    @PostMapping
    public ResponseEntity<AssignmentCreateResponse> createAssignment(
            @PathVariable Long academyId,
            @PathVariable Long lectureId,
            @AuthenticationPrincipal Long teacherId,
            @RequestBody AssignmentCreateRequest request
    ) {
        AssignmentCreateResponse response =
                assignmentService.createAssignment(academyId, lectureId, teacherId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 학생용 – 특정 강의의 과제 리스트 조회
     * GET /api/academies/{academyId}/lectures/{lectureId}/assignments/list
     */
    @GetMapping("list")
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

}