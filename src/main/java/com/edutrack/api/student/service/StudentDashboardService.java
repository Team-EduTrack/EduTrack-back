package com.edutrack.api.student.service;

import com.edutrack.api.student.dto.*;
import com.edutrack.api.student.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class StudentDashboardService {

    private final StudentLectureQueryRepository lectureQueryRepository;
    private final StudentAssignmentQueryRepository assignmentQueryRepository;
    private final StudentExamQueryRepository examQueryRepository;
    private final StudentAttendanceRepository attendanceRepository;

    public StudentDashboardService(StudentLectureQueryRepository lectureQueryRepository,
                                   StudentAssignmentQueryRepository assignmentQueryRepository,
                                   StudentExamQueryRepository examQueryRepository,
                                   StudentAttendanceRepository attendanceRepository) {
        this.lectureQueryRepository = lectureQueryRepository;
        this.assignmentQueryRepository = assignmentQueryRepository;
        this.examQueryRepository = examQueryRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<MyLectureResponse> getMyLectures(Long studentId) {
        return lectureQueryRepository.findMyLectures(studentId);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<AssignmentSummaryResponse> getMyAssignments(Long studentId) {
        return assignmentQueryRepository.findMyAssignments(studentId);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ExamSummaryResponse> getMyExams(Long studentId) {
        return examQueryRepository.findMyExams(studentId);
    }

    public AttendanceCheckInResponse checkIn(Long studentId) {
        LocalDate today = LocalDate.now();

        int count = attendanceRepository.countTodayAttendance(studentId, today);
        if (count == 0) {
            attendanceRepository.insertAttendance(studentId, today, true);
            return new AttendanceCheckInResponse(studentId, today, false);
        }

        //return new AttendanceCheckInResponse(studentId, today, true); // 확장 부분 -> 이미 출석 찍었으면 나오는 값
        return null;
    }
}
