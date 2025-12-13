package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.StudentLectureAttendanceResponse;
import com.edutrack.domain.statistics.service.StudentAttendanceService;
import com.edutrack.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 학생 리포트 컨트롤러
 * 
 * 현재 코드베이스의 복수 요일 지원 기능과 호환됩니다.
 * 
 */
@RestController
@RequiredArgsConstructor
public class StudentReportController {

    private final StudentAttendanceService studentAttendanceService;

    /**
     * 학생의 강의별 월간 출석 현황 조회
     * 
     * @param studentId 학생 ID
     * @param lectureId 강의 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param principalId 인증된 사용자 ID
     * @return 학생의 월별 출석 현황 정보
     */
    @GetMapping("/api/students/{studentId}/lectures/{lectureId}/attendance/monthly")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<StudentLectureAttendanceResponse> getMonthlyAttendance(
            @PathVariable Long studentId,
            @PathVariable Long lectureId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal Long principalId
    ) {
        if (!studentId.equals(principalId)) {
            throw new ForbiddenException("본인의 출석 현황만 조회할 수 있습니다.");
        }
        StudentLectureAttendanceResponse response = studentAttendanceService
                .getMonthlyAttendance(studentId, lectureId, year, month);
        return ResponseEntity.ok(response);
    }
}
