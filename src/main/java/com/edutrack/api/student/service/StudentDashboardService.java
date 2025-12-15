package com.edutrack.api.student.service;

import com.edutrack.api.student.dto.*;
import com.edutrack.api.student.repository.*;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 학생 대시보드 서비스
 * - 강의, 과제, 시험 목록 조회
 * - 출석 체크
 */
@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentLectureQueryRepository lectureQueryRepository;
    private final StudentAssignmentQueryRepository assignmentQueryRepository;
    private final StudentExamQueryRepository examQueryRepository;
    private final StudentAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    /**
     * 내 강의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MyLectureResponse> getMyLectures(Long studentId) {
        validateStudent(studentId);
        return lectureQueryRepository.findMyLectures(studentId);
    }

    /**
     * 내 과제 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AssignmentSummaryResponse> getMyAssignments(Long studentId) {
        validateStudent(studentId);
        return assignmentQueryRepository.findMyAssignments(studentId);
    }

    /**
     * 내 시험 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ExamSummaryResponse> getMyExams(Long studentId) {
        validateStudent(studentId);
        List<ExamStatus> statues = List.of(ExamStatus.PUBLISHED, ExamStatus.CLOSED);
        return examQueryRepository.findMyExams(studentId, statues);
    }

    /**
     * 출석 체크
     * - 이미 출석한 경우 alreadyCheckedIn = true 반환
     * - 처음 출석하는 경우 출석 기록 생성 후 alreadyCheckedIn = false 반환
     */
    @Transactional
    public AttendanceCheckInResponse checkIn(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. ID: " + studentId));

        LocalDate today = LocalDate.now();

        // 오늘 이미 출석했는지 확인
        if (attendanceRepository.existsByStudentIdAndDate(studentId, today)) {
            return new AttendanceCheckInResponse(studentId, today, true);
        }

        // 출석 기록 생성 (엔티티 기반)
        Attendance attendance = new Attendance(today, student);
        attendance.attend();
        attendanceRepository.save(attendance);

        return new AttendanceCheckInResponse(studentId, today, false);
    }


    /**
     * 학생 존재 여부 검증
     */
    private void validateStudent(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new NotFoundException("학생을 찾을 수 없습니다. ID: " + studentId);
        }
    }
}
