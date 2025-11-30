package com.edutrack.domain.assignment.controller;

import com.edutrack.domain.assignment.dto.AssignmentCreateRequest;
import com.edutrack.domain.assignment.dto.AssignmentCreateResponse;
import com.edutrack.domain.assignment.dto.AssignmentListResponse;
import com.edutrack.domain.assignment.service.AssignmentService;
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
            @PathVariable Long lectureId
    ) {
        List<AssignmentListResponse> responses =
                assignmentService.getAssignmentsForLecture(academyId, lectureId);

        return ResponseEntity.ok(responses);
    }
}