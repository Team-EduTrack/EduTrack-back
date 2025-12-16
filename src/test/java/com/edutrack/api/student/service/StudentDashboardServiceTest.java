package com.edutrack.api.student.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.StudentExamStatus;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.student.dto.AssignmentSummaryResponse;
import com.edutrack.domain.student.dto.AttendanceCheckInResponse;
import com.edutrack.domain.student.dto.ExamSummaryResponse;
import com.edutrack.domain.student.dto.MyLectureDetailResponse;
import com.edutrack.domain.student.dto.MyLectureResponse;
import com.edutrack.domain.student.repository.StudentAssignmentQueryRepository;
import com.edutrack.domain.student.repository.StudentAttendanceRepository;
import com.edutrack.domain.student.repository.StudentExamQueryRepository;
import com.edutrack.domain.student.repository.StudentLectureQueryRepository;
import com.edutrack.domain.student.service.StudentDashboardService;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;

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

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureStudentRepository lectureStudentRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamStudentRepository examStudentRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

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
                .description("수학의 기초 개념을 다루는 강의입니다.")
                .startDate(LocalDateTime.of(2025, 1, 1, 9, 0))
                .endDate(LocalDateTime.of(2025, 6, 30, 18, 0))
                .build();

        MyLectureResponse lecture2 = MyLectureResponse.builder()
                .lectureId(20L)
                .lectureTitle("영어 문법")
                .teacherName("박선생")
                .description("영어 문법의 핵심을 학습하는 강의입니다.")
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

        List<ExamStatus> statues = List.of(ExamStatus.PUBLISHED, ExamStatus.CLOSED);

        when(examQueryRepository.findMyExams(studentId, statues))
                .thenReturn(List.of(exam1, exam2));

        // when
        List<ExamSummaryResponse> result = studentDashboardService.getMyExams(studentId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("GRADED", result.get(0).getStatus());
        assertNull(result.get(1).getStatus());

        verify(userRepository).existsById(studentId);
        verify(examQueryRepository).findMyExams(studentId, statues);

        log.info("=== 내 시험 목록 조회 테스트 결과 ===");
        result.forEach(e -> log.info(
                "시험 ID: {}, 제목: {}, 점수: {}, 상태: {}",
                e.getExamId(), e.getTitle(), e.getEarnedScore(), e.getStatus()
        ));
    }

    @Test
    @DisplayName("내 강의 상세 조회 성공 - 모든 데이터 정상 조회")
    void 내강의_상세_조회_성공() {
        // given
        Long studentId = 1L;
        Long lectureId = 10L;
        Long teacherId = 100L;

        // 학생 존재 확인
        when(userRepository.existsById(studentId)).thenReturn(true);

        // 강의 Mock 생성
        Lecture lecture = mock(Lecture.class);
        when(lecture.getId()).thenReturn(lectureId);
        when(lecture.getTitle()).thenReturn("수학 기초");
        when(lecture.getDescription()).thenReturn("수학의 기초 개념을 다루는 강의입니다.");
        when(lecture.getStartDate()).thenReturn(LocalDateTime.of(2025, 1, 1, 9, 0));
        when(lecture.getEndDate()).thenReturn(LocalDateTime.of(2025, 6, 30, 18, 0));
        when(lecture.getDaysOfWeek()).thenReturn(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        User teacher = mock(User.class);
        lenient().when(teacher.getId()).thenReturn(teacherId); // ID는 현재 로직에서 직접 사용되지 않으므로 lenient 처리
        when(teacher.getName()).thenReturn("김선생");
        when(lecture.getTeacher()).thenReturn(teacher);

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).thenReturn(true);

        // 출석 데이터 Mock
        Attendance attendance1 = mock(Attendance.class);
        Attendance attendance2 = mock(Attendance.class);
        Attendance attendance3 = mock(Attendance.class);
        List<Attendance> attendances = List.of(attendance1, attendance2, attendance3);
        when(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                eq(studentId), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(attendances);

        // 과제 데이터 Mock
        Assignment assignment1 = mock(Assignment.class);
        when(assignment1.getId()).thenReturn(200L);
        // assignment1은 제출되어 필터링되므로 상세 필드는 lenient 처리
        lenient().when(assignment1.getTitle()).thenReturn("1단원 연습문제");
        lenient().when(assignment1.getStartDate()).thenReturn(LocalDateTime.of(2025, 1, 10, 0, 0));
        lenient().when(assignment1.getEndDate()).thenReturn(LocalDateTime.of(2025, 1, 17, 23, 59));

        Assignment assignment2 = mock(Assignment.class);
        when(assignment2.getId()).thenReturn(201L);
        when(assignment2.getTitle()).thenReturn("2단원 연습문제");
        when(assignment2.getStartDate()).thenReturn(LocalDateTime.of(2025, 1, 20, 0, 0));
        when(assignment2.getEndDate()).thenReturn(LocalDateTime.of(2025, 1, 27, 23, 59));

        Assignment assignment3 = mock(Assignment.class);
        when(assignment3.getId()).thenReturn(202L);
        when(assignment3.getTitle()).thenReturn("3단원 연습문제");
        when(assignment3.getStartDate()).thenReturn(LocalDateTime.of(2025, 2, 1, 0, 0));
        when(assignment3.getEndDate()).thenReturn(LocalDateTime.of(2025, 2, 8, 23, 59));

        List<Assignment> allAssignments = List.of(assignment1, assignment2, assignment3);
        when(assignmentRepository.findByLectureId(lectureId)).thenReturn(allAssignments);

        // 과제 제출 여부 Mock (assignment1만 제출, assignment2, assignment3는 미제출)
        when(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(200L, studentId)).thenReturn(true);
        when(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(201L, studentId)).thenReturn(false);
        when(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(202L, studentId)).thenReturn(false);

        // 시험 데이터 Mock
        Exam exam1 = mock(Exam.class);
        when(exam1.getId()).thenReturn(300L);
        // exam1은 SUBMITTED로 제외되므로 상세 필드는 lenient 처리
        lenient().when(exam1.getTitle()).thenReturn("중간고사");
        lenient().when(exam1.getStartDate()).thenReturn(LocalDateTime.of(2025, 3, 15, 9, 0));
        lenient().when(exam1.getEndDate()).thenReturn(LocalDateTime.of(2025, 3, 15, 11, 0));

        Exam exam2 = mock(Exam.class);
        when(exam2.getId()).thenReturn(301L);
        lenient().when(exam2.getTitle()).thenReturn("기말고사");
        lenient().when(exam2.getStartDate()).thenReturn(LocalDateTime.of(2025, 6, 20, 9, 0));
        lenient().when(exam2.getEndDate()).thenReturn(LocalDateTime.of(2025, 6, 20, 11, 0));

        Exam exam3 = mock(Exam.class);
        when(exam3.getId()).thenReturn(302L);
        when(exam3.getTitle()).thenReturn("단원 테스트");
        when(exam3.getStartDate()).thenReturn(LocalDateTime.of(2025, 2, 10, 10, 0));
        when(exam3.getEndDate()).thenReturn(LocalDateTime.of(2025, 2, 10, 11, 0));

        List<Exam> allExams = List.of(exam1, exam2, exam3);
        when(examRepository.findByLectureId(lectureId)).thenReturn(allExams);

        // 시험 응시 기록 Mock (exam1은 이미 제출함, exam2는 응시 중 → 둘 다 ExamStudent 존재하므로 제외됨, exam3는 아직 안 봄 → 포함됨)
        ExamStudent examStudent1 = mock(ExamStudent.class);
        when(examStudent1.getExam()).thenReturn(exam1);
        lenient().when(examStudent1.getStatus()).thenReturn(StudentExamStatus.SUBMITTED);

        ExamStudent examStudent2 = mock(ExamStudent.class);
        when(examStudent2.getExam()).thenReturn(exam2);
        lenient().when(examStudent2.getStatus()).thenReturn(StudentExamStatus.IN_PROGRESS);

        List<ExamStudent> examStudents = List.of(examStudent1, examStudent2);
        when(examStudentRepository.findAllByExamIdsAndStudentIds(
                eq(List.of(300L, 301L, 302L)), eq(List.of(studentId))
        )).thenReturn(examStudents);

        // when
        MyLectureDetailResponse result = studentDashboardService.getMyLectureDetail(studentId, lectureId);

        // then
        assertNotNull(result);
        assertEquals(lectureId, result.getLectureId());
        assertEquals("수학 기초", result.getLectureTitle());
        assertEquals("김선생", result.getTeacherName());
        assertEquals("수학의 기초 개념을 다루는 강의입니다.", result.getDescription());
        assertNotNull(result.getAttendanceRate());
        assertNotNull(result.getAssignmentSubmissionRate());
        assertEquals(33.33, result.getAssignmentSubmissionRate(), 0.1); // 1/3 = 33.33%

        // 시험 목록 검증 (exam3만 포함되어야 함 - exam1, exam2는 ExamStudent 존재하므로 제외)
        assertNotNull(result.getExams());
        assertEquals(1, result.getExams().size());
        assertEquals(302L, result.getExams().get(0).getExamId());
        assertEquals("단원 테스트", result.getExams().get(0).getExamTitle());

        // 과제 목록 검증 (assignment2, assignment3만 포함되어야 함 - assignment1은 이미 제출)
        assertNotNull(result.getAssignments());
        assertEquals(2, result.getAssignments().size());
        assertEquals(201L, result.getAssignments().get(0).getAssignmentId());
        assertEquals(202L, result.getAssignments().get(1).getAssignmentId());

        verify(userRepository).existsById(studentId);
        verify(lectureRepository).findById(lectureId);
        verify(lectureStudentRepository).existsByLecture_IdAndStudent_Id(lectureId, studentId);
        verify(attendanceRepository).findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                eq(studentId), any(LocalDate.class), any(LocalDate.class)
        );
        verify(examRepository).findByLectureId(lectureId);
        verify(examStudentRepository).findAllByExamIdsAndStudentIds(any(), any());

        log.info("=== 내 강의 상세 조회 테스트 결과 ===");
        log.info("강의 ID: {}, 제목: {}, 강사: {}", result.getLectureId(), result.getLectureTitle(), result.getTeacherName());
        log.info("출석률: {}%", result.getAttendanceRate());
        log.info("과제 제출률: {}% (제출: 1/3)", result.getAssignmentSubmissionRate());
        log.info("봐야 하는 시험 수: {} (exam3만 포함 - exam1, exam2는 이미 시작함)", result.getExams().size());
        result.getExams().forEach(e -> log.info("  - 시험 ID: {}, 제목: {}", e.getExamId(), e.getExamTitle()));
        log.info("제출해야 하는 과제 수: {} (assignment2, assignment3)", result.getAssignments().size());
        result.getAssignments().forEach(a -> log.info("  - 과제 ID: {}, 제목: {}", a.getAssignmentId(), a.getAssignmentTitle()));
    }

    @Test
    @DisplayName("내 강의 상세 조회 실패 - 존재하지 않는 학생")
    void 내강의_상세_조회_실패_존재하지_않는_학생() {
        // given
        Long invalidStudentId = 999L;
        Long lectureId = 10L;
        when(userRepository.existsById(invalidStudentId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> {
            studentDashboardService.getMyLectureDetail(invalidStudentId, lectureId);
        });

        verify(userRepository).existsById(invalidStudentId);
        verify(lectureRepository, never()).findById(any());

        log.info("=== 존재하지 않는 학생 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("내 강의 상세 조회 실패 - 존재하지 않는 강의")
    void 내강의_상세_조회_실패_존재하지_않는_강의() {
        // given
        Long studentId = 1L;
        Long invalidLectureId = 999L;
        when(userRepository.existsById(studentId)).thenReturn(true);
        when(lectureRepository.findById(invalidLectureId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            studentDashboardService.getMyLectureDetail(studentId, invalidLectureId);
        });

        verify(userRepository).existsById(studentId);
        verify(lectureRepository).findById(invalidLectureId);
        verify(lectureStudentRepository, never()).existsByLecture_IdAndStudent_Id(any(), any());

        log.info("=== 존재하지 않는 강의 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("내 강의 상세 조회 실패 - 수강 중이 아닌 강의")
    void 내강의_상세_조회_실패_수강_중이_아닌_강의() {
        // given
        Long studentId = 1L;
        Long lectureId = 10L;

        when(userRepository.existsById(studentId)).thenReturn(true);

        Lecture lecture = mock(Lecture.class);
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).thenReturn(false);

        // when & then
        assertThrows(ForbiddenException.class, () -> {
            studentDashboardService.getMyLectureDetail(studentId, lectureId);
        });

        verify(userRepository).existsById(studentId);
        verify(lectureRepository).findById(lectureId);
        verify(lectureStudentRepository).existsByLecture_IdAndStudent_Id(lectureId, studentId);
        verify(attendanceRepository, never()).findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(any(), any(), any());

        log.info("=== 수강 중이 아닌 강의 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("내 강의 상세 조회 성공 - 출석률 0% (출석 기록 없음)")
    void 내강의_상세_조회_성공_출석률_0퍼센트() {
        // given
        Long studentId = 1L;
        Long lectureId = 10L;
        Long teacherId = 100L;

        when(userRepository.existsById(studentId)).thenReturn(true);

        Lecture lecture = mock(Lecture.class);
        when(lecture.getId()).thenReturn(lectureId);
        when(lecture.getTitle()).thenReturn("수학 기초");
        when(lecture.getDescription()).thenReturn("수학의 기초 개념을 다루는 강의입니다.");
        when(lecture.getStartDate()).thenReturn(LocalDateTime.of(2025, 1, 1, 9, 0));
        when(lecture.getEndDate()).thenReturn(LocalDateTime.of(2025, 1, 31, 18, 0));
        when(lecture.getDaysOfWeek()).thenReturn(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        User teacher = mock(User.class);
        when(teacher.getName()).thenReturn("김선생");
        when(lecture.getTeacher()).thenReturn(teacher);

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).thenReturn(true);

        // 출석 기록 없음
        when(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                eq(studentId), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(List.of());

        // 과제 없음
        when(assignmentRepository.findByLectureId(lectureId)).thenReturn(List.of());

        // 시험 없음
        when(examRepository.findByLectureId(lectureId)).thenReturn(List.of());

        // when
        MyLectureDetailResponse result = studentDashboardService.getMyLectureDetail(studentId, lectureId);

        // then
        assertNotNull(result);
        assertEquals(0.0, result.getAttendanceRate());
        assertEquals(0.0, result.getAssignmentSubmissionRate());
        assertTrue(result.getExams().isEmpty());
        assertTrue(result.getAssignments().isEmpty());

        log.info("=== 출석률 0% 테스트 결과 ===");
        log.info("출석률: {}%", result.getAttendanceRate());
        log.info("과제 제출률: {}%", result.getAssignmentSubmissionRate());
    }

    @Test
    @DisplayName("내 강의 상세 조회 성공 - 과제 제출률 100% (모든 과제 제출)")
    void 내강의_상세_조회_성공_과제_제출률_100퍼센트() {
        // given
        Long studentId = 1L;
        Long lectureId = 10L;
        Long teacherId = 100L;

        when(userRepository.existsById(studentId)).thenReturn(true);

        Lecture lecture = mock(Lecture.class);
        when(lecture.getId()).thenReturn(lectureId);
        when(lecture.getTitle()).thenReturn("수학 기초");
        when(lecture.getDescription()).thenReturn("수학의 기초 개념을 다루는 강의입니다.");
        when(lecture.getStartDate()).thenReturn(LocalDateTime.of(2025, 1, 1, 9, 0));
        when(lecture.getEndDate()).thenReturn(LocalDateTime.of(2025, 1, 31, 18, 0));
        when(lecture.getDaysOfWeek()).thenReturn(List.of(DayOfWeek.MONDAY));

        User teacher = mock(User.class);
        when(teacher.getName()).thenReturn("김선생");
        when(lecture.getTeacher()).thenReturn(teacher);

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).thenReturn(true);

        // 출석 기록
        Attendance attendance = mock(Attendance.class);
        when(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                eq(studentId), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(List.of(attendance));

        // 모든 과제 제출 (제출률 계산에만 사용, 과제 목록 조회에는 사용되지 않음)
        Assignment assignment1 = mock(Assignment.class);
        when(assignment1.getId()).thenReturn(200L);

        Assignment assignment2 = mock(Assignment.class);
        when(assignment2.getId()).thenReturn(201L);

        List<Assignment> allAssignments = List.of(assignment1, assignment2);
        when(assignmentRepository.findByLectureId(lectureId)).thenReturn(allAssignments);
        when(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(200L, studentId)).thenReturn(true);
        when(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(201L, studentId)).thenReturn(true);

        // 시험 없음
        when(examRepository.findByLectureId(lectureId)).thenReturn(List.of());

        // when
        MyLectureDetailResponse result = studentDashboardService.getMyLectureDetail(studentId, lectureId);

        // then
        assertNotNull(result);
        assertEquals(100.0, result.getAssignmentSubmissionRate());
        assertTrue(result.getAssignments().isEmpty()); // 모든 과제 제출했으므로 빈 리스트

        log.info("=== 과제 제출률 100% 테스트 결과 ===");
        log.info("과제 제출률: {}% (제출: 2/2)", result.getAssignmentSubmissionRate());
        log.info("제출해야 하는 과제 수: {}", result.getAssignments().size());
    }

    @Test
    @DisplayName("내 강의 상세 조회 성공 - 모든 시험 이미 봄 (시험 목록 빈 리스트)")
    void 내강의_상세_조회_성공_모든_시험_이미_봄() {
        // given
        Long studentId = 1L;
        Long lectureId = 10L;
        Long teacherId = 100L;

        when(userRepository.existsById(studentId)).thenReturn(true);

        Lecture lecture = mock(Lecture.class);
        when(lecture.getId()).thenReturn(lectureId);
        when(lecture.getTitle()).thenReturn("수학 기초");
        when(lecture.getDescription()).thenReturn("수학의 기초 개념을 다루는 강의입니다.");
        when(lecture.getStartDate()).thenReturn(LocalDateTime.of(2025, 1, 1, 9, 0));
        when(lecture.getEndDate()).thenReturn(LocalDateTime.of(2025, 1, 31, 18, 0));
        when(lecture.getDaysOfWeek()).thenReturn(List.of(DayOfWeek.MONDAY));

        User teacher = mock(User.class);
        when(teacher.getName()).thenReturn("김선생");
        when(lecture.getTeacher()).thenReturn(teacher);

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).thenReturn(true);

        // 출석 기록
        Attendance attendance = mock(Attendance.class);
        when(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                eq(studentId), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(List.of(attendance));

        // 과제 없음
        when(assignmentRepository.findByLectureId(lectureId)).thenReturn(List.of());

        // 모든 시험 이미 봄 (ExamStudent가 존재하면 모두 제외됨)
        Exam exam1 = mock(Exam.class);
        when(exam1.getId()).thenReturn(300L);

        Exam exam2 = mock(Exam.class);
        when(exam2.getId()).thenReturn(301L);

        List<Exam> allExams = List.of(exam1, exam2);
        when(examRepository.findByLectureId(lectureId)).thenReturn(allExams);

        // 모든 시험 이미 시작함 (ExamStudent 존재 → 제외됨)
        ExamStudent examStudent1 = mock(ExamStudent.class);
        when(examStudent1.getExam()).thenReturn(exam1);
        lenient().when(examStudent1.getStatus()).thenReturn(StudentExamStatus.SUBMITTED);

        ExamStudent examStudent2 = mock(ExamStudent.class);
        when(examStudent2.getExam()).thenReturn(exam2);
        lenient().when(examStudent2.getStatus()).thenReturn(StudentExamStatus.GRADED);

        List<ExamStudent> examStudents = List.of(examStudent1, examStudent2);
        when(examStudentRepository.findAllByExamIdsAndStudentIds(
                eq(List.of(300L, 301L)), eq(List.of(studentId))
        )).thenReturn(examStudents);

        // when
        MyLectureDetailResponse result = studentDashboardService.getMyLectureDetail(studentId, lectureId);

        // then
        assertNotNull(result);
        assertTrue(result.getExams().isEmpty()); // 모든 시험 이미 봄

        log.info("=== 모든 시험 이미 봄 테스트 결과 ===");
        log.info("봐야 하는 시험 수: {}", result.getExams().size());
    }

}
