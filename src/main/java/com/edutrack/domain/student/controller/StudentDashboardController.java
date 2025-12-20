package com.edutrack.domain.student.controller;

import com.edutrack.domain.student.dto.AssignmentSummaryResponse;
import com.edutrack.domain.student.dto.AttendanceCheckInResponse;
import com.edutrack.domain.student.dto.ExamSummaryResponse;
import com.edutrack.domain.student.dto.MyLectureDetailResponse;
import com.edutrack.domain.student.dto.MyLectureResponse;
import com.edutrack.domain.student.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    /*
     * 내강의 조회
     */
    @GetMapping("/lectures")
    public ResponseEntity<List<MyLectureResponse>> getMyLectures (
            @AuthenticationPrincipal Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyLectures(studentId));
    }

    /*
    * 내 강의 상세 조회
     */
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<MyLectureDetailResponse> getMyLectureDetail(
            @AuthenticationPrincipal Long studentId,
            @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyLectureDetail(studentId, lectureId));
    }

    /*
     * 출석하기 버튼
     */
    @PostMapping("/attendance/check-in") // 가독성으로 수정!!
    public ResponseEntity<AttendanceCheckInResponse> checkIn(
            @AuthenticationPrincipal Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.checkIn(studentId));
    }


    /*
     * 과제 리스트
     */
    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentSummaryResponse>> getMyAssignments(
            @AuthenticationPrincipal Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyAssignments(studentId));
    }


    /*
     * 시험 리스트
     */
    @GetMapping("/exams")
    public ResponseEntity<List<ExamSummaryResponse>> getMyExams(
            @AuthenticationPrincipal Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyExams(studentId));
    }
}
