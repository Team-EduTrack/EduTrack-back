package com.edutrack.api.student.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.edutrack.api.student.dto.*;
import com.edutrack.api.student.repository.*;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 학생 대시보드 서비스 테스트
 * - 내 강의 조회, 출석 체크, 과제/시험 목록 조회
 */
@ExtendWith(MockitoExtension.class)
class StudentDashboardServiceTest {

    private static final Logger log = LoggerFactory.getLogger(StudentDashboardServiceTest.class);

    @InjectMocks
    private StudentDashboardService studentDashboardService;

    @Mock
    private StudentLectureQueryRepository lectureQueryRepository;

    @Mock
    private StudentAssignmentQueryRepository assignmentQueryRepository;

    @Mock
    private StudentExamQueryRepository examQueryRepository;

    @Mock
    private StudentAttendanceRepository attendanceRepository;

    @Mock
    private UserRepository userRepository;

    private User student;
    private Long studentId;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        student = mock(User.class);
        lenient().when(student.getId()).thenReturn(studentId);
        lenient().when(student.getName()).thenReturn("홍길동");
    }

    @Test
    @DisplayName("내 강의 목록 조회 성공")
    void 내강의_목록_조회_성공() {
        // given
        when(userRepository.existsById(studentId)).thenReturn(true);

        // 빌더 패턴으로 DTO 생성
        MyLectureResponse lecture1 = MyLectureResponse.builder()
                .lectureId(10L)
                .lectureTitle("수학 기초")
                .teacherName("김선생")
                .startDate(LocalDateTime.of(2025, 1, 1, 9, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 18, 0))
                .build();

        MyLectureResponse lecture2 = MyLectureResponse.builder()
                .lectureId(20L)
                .lectureTitle("영어 문법")
                .teacherName("박선생")
                .startDate(LocalDateTime.of(2025, 2, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 7, 31, 17, 0))
                .build();

        when(lectureQueryRepository.findMyLectures(studentId))
                .thenReturn(List.of(lecture1, lecture2));

        // when
        List<MyLectureResponse> result = studentDashboardService.getMyLectures(studentId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("수학 기초", result.get(0).getLectureTitle());
        assertEquals("영어 문법", result.get(1).getLectureTitle());

        verify(userRepository).existsById(studentId);
        verify(lectureQueryRepository).findMyLectures(studentId);

        log.info("=== 내 강의 목록 조회 테스트 결과 ===");
        result.forEach(l -> log.info(
                "강의 ID: {}, 제목: {}, 선생님: {}",
                l.getLectureId(), l.getLectureTitle(), l.getTeacherName()
        ));
    }

    @Test
    @DisplayName("내 강의 목록 조회 실패 - 존재하지 않는 학생")
    void 내강의_목록_조회_실패_존재하지_않는_학생() {
        // given
        Long invalidStudentId = 999L;
        when(userRepository.existsById(invalidStudentId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> {
            studentDashboardService.getMyLectures(invalidStudentId);
        });

        verify(userRepository).existsById(invalidStudentId);
        verify(lectureQueryRepository, never()).findMyLectures(any());

        log.info("=== 존재하지 않는 학생 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("출석 체크 성공 - 첫 출석")
    void 출석_체크_성공_첫출석() {
        // given
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(attendanceRepository.existsByStudentIdAndDate(eq(studentId), any(LocalDate.class)))
                .thenReturn(false);

        ArgumentCaptor<Attendance> attendanceCaptor = ArgumentCaptor.forClass(Attendance.class);

        // when
        AttendanceCheckInResponse result = studentDashboardService.checkIn(studentId);

        // then
        assertNotNull(result);
        assertEquals(studentId, result.studentId());
        assertFalse(result.alreadyCheckedIn());
        assertEquals(LocalDate.now(), result.attendanceDate());

        verify(userRepository).findById(studentId);
        verify(attendanceRepository).existsByStudentIdAndDate(eq(studentId), any(LocalDate.class));
        verify(attendanceRepository).save(attendanceCaptor.capture());

        Attendance savedAttendance = attendanceCaptor.getValue();
        assertNotNull(savedAttendance);

        log.info("=== 첫 출석 체크 테스트 결과 ===");
        log.info("학생 ID: {}, 출석일: {}, 이미 출석함: {}",
                result.studentId(), result.attendanceDate(), result.alreadyCheckedIn());
    }

    @Test
    @DisplayName("출석 체크 성공 - 이미 출석한 경우")
    void 출석_체크_성공_이미_출석() {
        // given
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(attendanceRepository.existsByStudentIdAndDate(eq(studentId), any(LocalDate.class)))
                .thenReturn(true);

        // when
        AttendanceCheckInResponse result = studentDashboardService.checkIn(studentId);

        // then
        assertNotNull(result);
        assertEquals(studentId, result.studentId());
        assertTrue(result.alreadyCheckedIn());

        verify(userRepository).findById(studentId);
        verify(attendanceRepository).existsByStudentIdAndDate(eq(studentId), any(LocalDate.class));
        verify(attendanceRepository, never()).save(any());

        log.info("=== 이미 출석한 경우 테스트 결과 ===");
        log.info("학생 ID: {}, 이미 출석함: {}", result.studentId(), result.alreadyCheckedIn());
    }

    @Test
    @DisplayName("내 과제 목록 조회 성공")
    void 내과제_목록_조회_성공() {
        // given
        when(userRepository.existsById(studentId)).thenReturn(true);

        // 빌더 패턴으로 DTO 생성
        AssignmentSummaryResponse assignment1 = AssignmentSummaryResponse.builder()
                .assignmentId(100L)
                .lectureTitle("수학 기초")
                .title("1단원 연습문제")
                .startDate(LocalDateTime.of(2025, 1, 10, 0, 0))
                .endDate(LocalDateTime.of(2025, 1, 17, 23, 59))
                .score(85)
                .build();

        AssignmentSummaryResponse assignment2 = AssignmentSummaryResponse.builder()
                .assignmentId(101L)
                .lectureTitle("영어 문법")
                .title("문법 퀴즈")
                .startDate(LocalDateTime.of(2025, 1, 15, 0, 0))
                .endDate(LocalDateTime.of(2025, 1, 22, 23, 59))
                .score(null)  // 미제출
                .build();

        when(assignmentQueryRepository.findMyAssignments(studentId))
                .thenReturn(List.of(assignment1, assignment2));

        // when
        List<AssignmentSummaryResponse> result = studentDashboardService.getMyAssignments(studentId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(85, result.get(0).getScore());
        assertNull(result.get(1).getScore());

        verify(userRepository).existsById(studentId);
        verify(assignmentQueryRepository).findMyAssignments(studentId);

        log.info("=== 내 과제 목록 조회 테스트 결과 ===");
        result.forEach(a -> log.info(
                "과제 ID: {}, 제목: {}, 점수: {}",
                a.getAssignmentId(), a.getTitle(), a.getScore()
        ));
    }

    @Test
    @DisplayName("내 시험 목록 조회 성공")
    void 내시험_목록_조회_성공() {
        // given
        when(userRepository.existsById(studentId)).thenReturn(true);

        // 빌더 패턴으로 DTO 생성
        ExamSummaryResponse exam1 = ExamSummaryResponse.builder()
                .examId(200L)
                .lectureTitle("수학 기초")
                .title("중간고사")
                .startDate(LocalDateTime.of(2025, 3, 15, 9, 0))
                .endDate(LocalDateTime.of(2025, 3, 15, 11, 0))
                .earnedScore(78)
                .status("GRADED")
                .build();

        ExamSummaryResponse exam2 = ExamSummaryResponse.builder()
                .examId(201L)
                .lectureTitle("영어 문법")
                .title("단원 테스트")
                .startDate(LocalDateTime.of(2025, 4, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 4, 1, 11, 0))
                .earnedScore(null)
                .status(null)  // 미응시
                .build();

        when(examQueryRepository.findMyExams(studentId))
                .thenReturn(List.of(exam1, exam2));

        // when
        List<ExamSummaryResponse> result = studentDashboardService.getMyExams(studentId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("GRADED", result.get(0).getStatus());
        assertNull(result.get(1).getStatus());

        verify(userRepository).existsById(studentId);
        verify(examQueryRepository).findMyExams(studentId);

        log.info("=== 내 시험 목록 조회 테스트 결과 ===");
        result.forEach(e -> log.info(
                "시험 ID: {}, 제목: {}, 점수: {}, 상태: {}",
                e.getExamId(), e.getTitle(), e.getEarnedScore(), e.getStatus()
        ));
    }
}
