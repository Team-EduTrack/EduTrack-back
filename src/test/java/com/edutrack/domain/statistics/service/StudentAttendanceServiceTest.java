package com.edutrack.domain.statistics.service;

import com.edutrack.api.student.repository.StudentAttendanceRepository;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.statistics.dto.StudentLectureAttendanceResponse;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudentAttendanceServiceTest {

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
    private int year;
    private int month;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        lectureId = 1L;
        year = 2025;
        month = 12;
    }

    @Nested
    @DisplayName("getMonthlyAttendance 메서드")
    class GetMonthlyAttendance {

        @Test
        @DisplayName("성공: 월간 출석 현황을 올바르게 반환한다")
        void success_returnsMonthlyAttendance() {
            // given
            Long principalId = studentId; // 본인 조회
            given(userRepository.existsById(studentId)).willReturn(true);
            given(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).willReturn(true);

            Lecture lecture = createLecture(lectureId, "영문법 특강", DayOfWeek.MONDAY);
            given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));

            // 2025년 12월 월요일: 1, 8, 15, 22, 29 (5일)
            LocalDate startDate = LocalDate.of(2025, 12, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            // 본인 출석: 1, 8, 15, 22 (4일)
            List<Attendance> myAttendances = Arrays.asList(
                    createAttendance(studentId, LocalDate.of(2025, 12, 1)),
                    createAttendance(studentId, LocalDate.of(2025, 12, 8)),
                    createAttendance(studentId, LocalDate.of(2025, 12, 15)),
                    createAttendance(studentId, LocalDate.of(2025, 12, 22))
            );
            given(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                    studentId, startDate, endDate)).willReturn(myAttendances);

            // 다른 수강생 없음
            given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(
                    Collections.singletonList(createLectureStudent(lectureId, studentId)));

            // when
            StudentLectureAttendanceResponse response = studentAttendanceService
                    .getMonthlyAttendance(studentId, lectureId, year, month, principalId);

            // then
            assertThat(response.getLectureId()).isEqualTo(lectureId);
            assertThat(response.getLectureName()).isEqualTo("영문법 특강");
            assertThat(response.getYear()).isEqualTo(2025);
            assertThat(response.getMonth()).isEqualTo(12);
            assertThat(response.getTotalClassDays()).isEqualTo(5);
            assertThat(response.getAttendedDays()).isEqualTo(4);
            assertThat(response.getAttendanceRate()).isEqualTo(80.0);
            assertThat(response.getAttendedDates()).hasSize(4);
        }

        @Test
        @DisplayName("성공: 다른 수강생 평균 출석률을 올바르게 계산한다 (배치 조회)")
        void success_calculatesOtherStudentsAvgAttendanceRate() {
            // given
            Long principalId = studentId; // 본인 조회
            Long otherStudentId = 2L;

            given(userRepository.existsById(studentId)).willReturn(true);
            given(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).willReturn(true);

            Lecture lecture = createLecture(lectureId, "영문법 특강", DayOfWeek.MONDAY);
            given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));

            LocalDate startDate = LocalDate.of(2025, 12, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            // 본인 출석: 4일
            List<Attendance> myAttendances = Arrays.asList(
                    createAttendance(studentId, LocalDate.of(2025, 12, 1)),
                    createAttendance(studentId, LocalDate.of(2025, 12, 8)),
                    createAttendance(studentId, LocalDate.of(2025, 12, 15)),
                    createAttendance(studentId, LocalDate.of(2025, 12, 22))
            );
            given(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                    studentId, startDate, endDate)).willReturn(myAttendances);

            // 다른 수강생 출석: 5일 (100%) - 배치 조회용
            List<Attendance> otherAttendances = Arrays.asList(
                    createAttendance(otherStudentId, LocalDate.of(2025, 12, 1)),
                    createAttendance(otherStudentId, LocalDate.of(2025, 12, 8)),
                    createAttendance(otherStudentId, LocalDate.of(2025, 12, 15)),
                    createAttendance(otherStudentId, LocalDate.of(2025, 12, 22)),
                    createAttendance(otherStudentId, LocalDate.of(2025, 12, 29))
            );
            // 배치 조회 mock
            given(attendanceRepository.findByStudentIdInAndDateBetweenAndStatusTrueOrderByDateAsc(
                    Arrays.asList(otherStudentId), startDate, endDate)).willReturn(otherAttendances);

            // 수강생 목록
            given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(Arrays.asList(
                    createLectureStudent(lectureId, studentId),
                    createLectureStudent(lectureId, otherStudentId)
            ));

            // when
            StudentLectureAttendanceResponse response = studentAttendanceService
                    .getMonthlyAttendance(studentId, lectureId, year, month, principalId);

            // then
            assertThat(response.getAttendanceRate()).isEqualTo(80.0);
            assertThat(response.getOtherStudentsAvgAttendanceRate()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("성공: 출석 기록이 없으면 출석률 0을 반환한다")
        void success_returnsZeroWhenNoAttendance() {
            // given
            Long principalId = studentId; // 본인 조회
            given(userRepository.existsById(studentId)).willReturn(true);
            given(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).willReturn(true);

            Lecture lecture = createLecture(lectureId, "영문법 특강", DayOfWeek.MONDAY);
            given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));

            LocalDate startDate = LocalDate.of(2025, 12, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            given(attendanceRepository.findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
                    studentId, startDate, endDate)).willReturn(Collections.emptyList());
            given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(
                    Collections.singletonList(createLectureStudent(lectureId, studentId)));

            // when
            StudentLectureAttendanceResponse response = studentAttendanceService
                    .getMonthlyAttendance(studentId, lectureId, year, month, principalId);

            // then
            assertThat(response.getAttendedDays()).isEqualTo(0);
            assertThat(response.getAttendanceRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("실패: 타인의 출석 현황 조회 시 ForbiddenException 발생")
        void fail_throwsForbiddenExceptionWhenNotOwner() {
            // given
            Long principalId = 999L; // 다른 사용자

            // when & then
            assertThatThrownBy(() -> studentAttendanceService.getMonthlyAttendance(studentId, lectureId, year, month, principalId))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("본인의 출석 현황만 조회할 수 있습니다");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 학생 ID로 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenStudentNotExists() {
            // given
            Long principalId = studentId; // 본인 조회
            given(userRepository.existsById(studentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> studentAttendanceService.getMonthlyAttendance(studentId, lectureId, year, month, principalId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("학생을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("실패: 강의에 등록되지 않은 학생이 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenNotEnrolled() {
            // given
            Long principalId = studentId; // 본인 조회
            given(userRepository.existsById(studentId)).willReturn(true);
            given(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> studentAttendanceService.getMonthlyAttendance(studentId, lectureId, year, month, principalId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("해당 강의에 등록된 학생이 아닙니다");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 강의 ID로 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenLectureNotExists() {
            // given
            Long principalId = studentId; // 본인 조회
            given(userRepository.existsById(studentId)).willReturn(true);
            given(lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)).willReturn(true);
            given(lectureRepository.findById(lectureId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> studentAttendanceService.getMonthlyAttendance(studentId, lectureId, year, month, principalId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("강의를 찾을 수 없습니다");
        }
    }

    // === Helper Methods ===

    private Lecture createLecture(Long id, String title, DayOfWeek dayOfWeek) {
        Lecture lecture = Lecture.builder()
                .title(title)
                .dayOfWeek(dayOfWeek)
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                .build();
        ReflectionTestUtils.setField(lecture, "id", id);
        return lecture;
    }

    private Attendance createAttendance(Long studentId, LocalDate date) {
        Attendance attendance = new Attendance();
        ReflectionTestUtils.setField(attendance, "date", date);
        ReflectionTestUtils.setField(attendance, "status", true);

        User student = User.builder().build();
        ReflectionTestUtils.setField(student, "id", studentId);
        ReflectionTestUtils.setField(attendance, "student", student);

        return attendance;
    }

    private LectureStudent createLectureStudent(Long lectureId, Long studentId) {
        User student = User.builder().build();
        ReflectionTestUtils.setField(student, "id", studentId);

        LectureStudent lectureStudent = new LectureStudent();
        ReflectionTestUtils.setField(lectureStudent, "student", student);

        return lectureStudent;
    }
}
