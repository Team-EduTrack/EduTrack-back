package com.edutrack.domain.student.controller;

import com.edutrack.domain.student.dto.AssignmentSummaryResponse;
import com.edutrack.domain.student.dto.AttendanceCheckInResponse;
import com.edutrack.domain.student.dto.ExamSummaryResponse;
import com.edutrack.domain.student.dto.MyLectureResponse;
import com.edutrack.domain.student.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    /*
     * 내강의 조회
     */
    @GetMapping("/{studentId}/lectures")
    public ResponseEntity<List<MyLectureResponse>> getMyLectures (
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyLectures(studentId));
    }

    /*
     * 출석하기 버튼
     */
    @PostMapping("/{studentId}/attendance/check-in") // 가독성으로 수정!!
    public ResponseEntity<AttendanceCheckInResponse> checkIn(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.checkIn(studentId));
    }


    /*
     * 과제 리스트
     */
    @GetMapping("/{studentId}/assignments")
    public ResponseEntity<List<AssignmentSummaryResponse>> getMyAssignments(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyAssignments(studentId));
    }


    /*
     * 시험 리스트
     */
    @GetMapping("/{studentId}/exams")
    public ResponseEntity<List<ExamSummaryResponse>> getMyExams(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(studentDashboardService.getMyExams(studentId));
    }
}
