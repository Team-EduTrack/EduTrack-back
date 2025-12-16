package com.edutrack.domain.student.controller;

import com.edutrack.domain.student.dto.*;
import com.edutrack.domain.student.dto.AssignmentSummaryResponse;
import com.edutrack.domain.student.dto.AttendanceCheckInResponse;
import com.edutrack.domain.student.dto.ExamSummaryResponse;
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


    //TODO : 해당 부분에 studentDashboardService에는 getMonthlyAttendance 메소드가 존재하지 않습니다.
  // TODO : 해당 부분 고려하셔서 컨트롤러 메소드 다시 작성 부탁드립니다.
  // TODO : {StudentId} 라는 @PathVariable 어노테이션을 사용하는 방향은 보안상 위험하다고 판단되니 수정된 다른 컨트롤러 메소드 확인 부탁드립니다.

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
