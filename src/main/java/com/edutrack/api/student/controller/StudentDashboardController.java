package com.edutrack.api.student.controller;

import com.edutrack.api.student.dto.*;
import com.edutrack.api.student.service.StudentDashboardService;
import com.edutrack.domain.statistics.service.StudentAttendanceService;
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


    //TODO : 해당 부분에 studentDashboardService에는 getMonthlyAttendance 메소드가 존재하지 않습니다.
  // TODO : 해당 부분 고려하셔서 컨트롤러 메소드 다시 작성 부탁드립니다.

//    /*
//     * 강의별 월별 출석률 조회
//     */
//    @GetMapping("/{studentId}/lectures/{lectureId}/attendance/monthly")
//    public ResponseEntity<MonthlyAttendanceResponse> getMonthlyAttendance(
//            @PathVariable Long studentId,
//            @PathVariable Long lectureId,
//            @RequestParam int year,
//            @RequestParam int month
//    ) {
//        return ResponseEntity.ok(studentDashboardService.getMonthlyAttendance(
//                studentId, lectureId, year, month));
//    }

}
