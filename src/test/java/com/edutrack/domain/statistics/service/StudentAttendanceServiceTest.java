package com.edutrack.domain.statistics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.edutrack.domain.student.repository.StudentAttendanceRepository;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.statistics.dto.StudentLectureAttendanceResponse;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * 학생 출석 서비스 테스트
 * 복수 요일 지원 테스트 포함
 */
@ExtendWith(MockitoExtension.class)
class StudentAttendanceServiceTest {

    private static final Logger log = LoggerFactory.getLogger(StudentAttendanceServiceTest.class);

    @InjectMocks
    private StudentAttendanceService studentAttendanceService;

    @Mock
    private StudentAttendanceRepository attendanceRepository;

    @Mock
    private LectureStudentRepository lectureStudentRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private UserRepository userRepository;

    private Long studentId;
    private Long lectureId;
    private Lecture lecture;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        lectureId = 10L;

        // 강의 생성 (월, 수, 금 수업)
        lecture = mock(Lecture.class);
        when(lecture.getId()).thenReturn(lectureId);
        when(lecture.getTitle()).thenReturn("영문법 특강");
        when(lecture.getDaysOfWeek()).thenReturn(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        when(lecture.getStartDate()).thenReturn(LocalDateTime.of(2025, 11, 1, 9, 0));
        when(lecture.getEndDate()).thenReturn(LocalDateTime.of(2025, 12, 31, 18, 0));
    }

    @Test
    @DisplayName("강의별 월별 출석률 조회 성공 - 복수 요일 지원")
    void 강의별_월별_출석률_조회_성공() {
        // given
        int year = 2025;
        int month = 12;

        // 학생 존재 확인
        when(userRepository.existsById(studentId)).thenReturn(true);

        // 학생이 강의에 등록되어 있음
        LectureStudentId lectureStudentId = new LectureStudentId(lectureId, studentId);
        when(lectureStudentRepository.existsById(lectureStudentId))
                .thenReturn(true);

        // 강의 정보 조회
        when(lectureRepository.findById(lectureId))
                .thenReturn(Optional.of(lecture));

        // 출석 기록 생성 (12월 2일(월), 9일(월), 16일(월) 출석)
        Attendance attendance1 = mock(Attendance.class);
        when(attendance1.getDate()).thenReturn(LocalDate.of(2025, 12, 2)); // 월요일
        when(attendance1.isStatus()).thenReturn(true);

        Attendance attendance2 = mock(Attendance.class);
        when(attendance2.getDate()).thenReturn(LocalDate.of(2025, 12, 9)); // 월요일
        when(attendance2.isStatus()).thenReturn(true);

        Attendance attendance3 = mock(Attendance.class);
        when(attendance3.getDate()).thenReturn(LocalDate.of(2025, 12, 16)); // 월요일
        when(attendance3.isStatus()).thenReturn(true);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                studentId, startDate, endDate))
                .thenReturn(Arrays.asList(attendance1, attendance2, attendance3));

        // 다른 학생들의 출석 기록 (평균 계산용)
        User otherStudent = mock(User.class);
        when(otherStudent.getId()).thenReturn(2L);

        LectureStudent otherLectureStudent = mock(LectureStudent.class);
        when(otherLectureStudent.getStudent()).thenReturn(otherStudent);

        when(lectureStudentRepository.findAllByLectureId(lectureId))
                .thenReturn(Arrays.asList(otherLectureStudent));

        Attendance otherAttendance1 = mock(Attendance.class);
        when(otherAttendance1.getDate()).thenReturn(LocalDate.of(2025, 12, 2));
        when(otherAttendance1.getStudent()).thenReturn(otherStudent);
        when(otherAttendance1.isStatus()).thenReturn(true);

        Attendance otherAttendance2 = mock(Attendance.class);
        when(otherAttendance2.getDate()).thenReturn(LocalDate.of(2025, 12, 9));
        when(otherAttendance2.getStudent()).thenReturn(otherStudent);
        when(otherAttendance2.isStatus()).thenReturn(true);

        when(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                eq(2L), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList(otherAttendance1, otherAttendance2));

        // when
        StudentLectureAttendanceResponse result = studentAttendanceService.getMonthlyAttendance(
                studentId, lectureId, year, month);

        // then
        assertNotNull(result);
        assertEquals(lectureId, result.getLectureId());
        assertEquals("영문법 특강", result.getLectureName());
        assertEquals(year, result.getYear());
        assertEquals(month, result.getMonth());
        assertTrue(result.getAttendedDates().contains(LocalDate.of(2025, 12, 2)));
        assertTrue(result.getAttendedDates().contains(LocalDate.of(2025, 12, 9)));
        assertTrue(result.getAttendedDates().contains(LocalDate.of(2025, 12, 16)));
        assertTrue(result.getTotalClassDays() > 0);
        assertEquals(3, result.getAttendedDays());
        assertTrue(result.getAttendanceRate() > 0);
        assertTrue(result.getOtherStudentsAvgAttendanceRate() >= 0);

        verify(userRepository).existsById(studentId);
        verify(lectureStudentRepository).existsById(lectureStudentId);
        verify(lectureRepository).findById(lectureId);
        verify(attendanceRepository).findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                studentId, startDate, endDate);

        log.info("=== 강의별 월별 출석률 조회 테스트 결과 ===");
        log.info("강의 ID: {}, 강의명: {}, 출석률: {}%, 다른 학생 평균: {}%",
                result.getLectureId(), result.getLectureName(),
                result.getAttendanceRate(), result.getOtherStudentsAvgAttendanceRate());
    }

    @Test
    @DisplayName("강의별 월별 출석률 조회 실패 - 수강하지 않는 강의")
    void 강의별_월별_출석률_조회_실패_수강하지_않는_강의() {
        // given
        int year = 2025;
        int month = 12;

        when(userRepository.existsById(studentId)).thenReturn(true);
        LectureStudentId lectureStudentId = new LectureStudentId(lectureId, studentId);
        when(lectureStudentRepository.existsById(lectureStudentId))
                .thenReturn(false);

        // when & then
        assertThrows(ForbiddenException.class, () -> {
            studentAttendanceService.getMonthlyAttendance(studentId, lectureId, year, month);
        });

        verify(userRepository).existsById(studentId);
        verify(lectureStudentRepository).existsById(lectureStudentId);
        verify(lectureRepository, never()).findById(any());

        log.info("=== 수강하지 않는 강의 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("강의별 월별 출석률 조회 실패 - 존재하지 않는 강의")
    void 강의별_월별_출석률_조회_실패_존재하지_않는_강의() {
        // given
        int year = 2025;
        int month = 12;

        when(userRepository.existsById(studentId)).thenReturn(true);
        LectureStudentId lectureStudentId = new LectureStudentId(lectureId, studentId);
        when(lectureStudentRepository.existsById(lectureStudentId))
                .thenReturn(true);
        when(lectureRepository.findById(lectureId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            studentAttendanceService.getMonthlyAttendance(studentId, lectureId, year, month);
        });

        verify(userRepository).existsById(studentId);
        verify(lectureStudentRepository).existsById(lectureStudentId);
        verify(lectureRepository).findById(lectureId);

        log.info("=== 존재하지 않는 강의 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("강의별 월별 출석률 조회 실패 - 존재하지 않는 학생")
    void 강의별_월별_출석률_조회_실패_존재하지_않는_학생() {
        // given
        int year = 2025;
        int month = 12;
        Long invalidStudentId = 999L;

        when(userRepository.existsById(invalidStudentId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> {
            studentAttendanceService.getMonthlyAttendance(invalidStudentId, lectureId, year, month);
        });

        verify(userRepository).existsById(invalidStudentId);
        verify(lectureStudentRepository, never()).existsById(any());

        log.info("=== 존재하지 않는 학생 조회 시 예외 발생 확인 ===");
    }
}
