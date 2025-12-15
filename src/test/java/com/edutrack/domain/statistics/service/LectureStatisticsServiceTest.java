package com.edutrack.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edutrack.domain.student.repository.StudentAttendanceRepository;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.lecture.dto.LectureStatisticsResponse;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.lecture.service.LectureHelper;
import com.edutrack.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("LectureStatisticsService 테스트")
class LectureStatisticsServiceTest {

  private static final Logger log = LoggerFactory.getLogger(LectureStatisticsServiceTest.class);

  @InjectMocks
  private LectureStatisticsService lectureStatisticsService;

  @Mock
  private LectureStudentRepository lectureStudentRepository;

  @Mock
  private StudentAttendanceRepository studentAttendanceRepository;

  @Mock
  private AssignmentRepository assignmentRepository;

  @Mock
  private AssignmentSubmissionRepository assignmentSubmissionRepository;

  @Mock
  private ExamRepository examRepository;

  @Mock
  private ExamStudentRepository examStudentRepository;

  @Mock
  private LectureHelper lectureHelper;

  private Long lectureId;
  private Long teacherId;
  private Lecture lecture;

  @BeforeEach
  void setUp() {
    lectureId = 1L;
    teacherId = 10L;

    lecture = mock(Lecture.class);
  }

  @Nested
  @DisplayName("getLecutureStatistics 메서드")
  class GetLecutureStatisticsTest {

    @Test
    @DisplayName("성공: 모든 통계가 정상적으로 계산된다")
    void success_calculatesAllStatisticsCorrectly() {
      log.info("=== 모든 통계 계산 테스트 시작 ===");
      log.info("강의 ID: {}, 강사 ID: {}", lectureId, teacherId);
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      User student2 = createStudent(2L);
      User student3 = createStudent(3L);

      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      LectureStudent lectureStudent2 = createLectureStudent(lecture, student2);
      LectureStudent lectureStudent3 = createLectureStudent(lecture, student3);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1, lectureStudent2, lectureStudent3);

      Assignment assignment1 = createAssignment(1L, lecture);
      Assignment assignment2 = createAssignment(2L, lecture);
      List<Assignment> assignments = List.of(assignment1, assignment2);

      Exam exam1 = createExam(1L, lecture);
      Exam exam2 = createExam(2L, lecture);
      List<Exam> exams = List.of(exam1, exam2);

      // 출석 가능 날짜: 2024-01-01(월), 2024-01-08(월), 2024-01-15(월), 2024-01-22(월), 2024-01-29(월) = 5일
      // 학생1: 5일 중 4일 출석 (80%)
      Attendance attendance1_1 = createAttendance(student1, LocalDate.of(2024, 1, 1), true);
      Attendance attendance1_2 = createAttendance(student1, LocalDate.of(2024, 1, 8), true);
      Attendance attendance1_3 = createAttendance(student1, LocalDate.of(2024, 1, 15), true);
      Attendance attendance1_4 = createAttendance(student1, LocalDate.of(2024, 1, 22), true);

      // 학생2: 5일 중 3일 출석 (60%)
      Attendance attendance2_1 = createAttendance(student2, LocalDate.of(2024, 1, 1), true);
      Attendance attendance2_2 = createAttendance(student2, LocalDate.of(2024, 1, 8), true);
      Attendance attendance2_3 = createAttendance(student2, LocalDate.of(2024, 1, 15), true);

      // 학생3: 5일 중 5일 출석 (100%)
      Attendance attendance3_1 = createAttendance(student3, LocalDate.of(2024, 1, 1), true);
      Attendance attendance3_2 = createAttendance(student3, LocalDate.of(2024, 1, 8), true);
      Attendance attendance3_3 = createAttendance(student3, LocalDate.of(2024, 1, 15), true);
      Attendance attendance3_4 = createAttendance(student3, LocalDate.of(2024, 1, 22), true);
      Attendance attendance3_5 = createAttendance(student3, LocalDate.of(2024, 1, 29), true);

      List<LocalDate> attendanceDates = List.of(
          LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15),
          LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)
      );
      List<Attendance> allAttendances = List.of(
          attendance1_1, attendance1_2, attendance1_3, attendance1_4,
          attendance2_1, attendance2_2, attendance2_3,
          attendance3_1, attendance3_2, attendance3_3, attendance3_4, attendance3_5
      );
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L, 2L, 3L), attendanceDates))
          .willReturn(allAttendances);

      // 평균 출석률: (80 + 60 + 100) / 3 = 80%

      // 과제 제출: 학생1은 과제1, 과제2 모두 제출, 학생2는 과제1만 제출, 학생3은 과제2만 제출
      // 총 가능 제출: 3명 × 2과제 = 6개
      // 실제 제출: 4개
      // 제출률: 4/6 * 100 = 66.67%
      AssignmentSubmission submission1_1 = createAssignmentSubmission(assignment1, student1);
      AssignmentSubmission submission1_2 = createAssignmentSubmission(assignment2, student1);
      AssignmentSubmission submission2_1 = createAssignmentSubmission(assignment1, student2);
      AssignmentSubmission submission3_2 = createAssignmentSubmission(assignment2, student3);
      given(assignmentSubmissionRepository.findAllByAssignmentIdsAndStudentIds(
          List.of(1L, 2L), List.of(1L, 2L, 3L)))
          .willReturn(List.of(submission1_1, submission1_2, submission2_1, submission3_2));

      // 시험 응시: 학생1은 시험1, 시험2 모두 응시, 학생2는 시험1만 응시, 학생3은 시험2만 응시
      // 총 가능 응시: 3명 × 2시험 = 6개
      // 실제 응시: 4개
      // 응시율: 4/6 * 100 = 66.67%
      ExamStudent exam1Student1 = createExamStudent(exam1, student1, 80);
      ExamStudent exam1Student2 = createExamStudent(exam1, student2, 70);
      ExamStudent exam2Student1 = createExamStudent(exam2, student1, 90);
      ExamStudent exam2Student3 = createExamStudent(exam2, student3, 85);

      // 평균 점수 계산용: 모든 시험의 모든 응시 기록
      given(examStudentRepository.findAllByExamIds(List.of(1L, 2L)))
          .willReturn(List.of(exam1Student1, exam1Student2, exam2Student1, exam2Student3));

      // 시험 응시율 및 상위 10% 계산용: 특정 학생들의 응시 기록
      given(examStudentRepository.findAllByExamIdsAndStudentIds(
          List.of(1L, 2L), List.of(1L, 2L, 3L)))
          .willReturn(List.of(exam1Student1, exam1Student2, exam2Student1, exam2Student3));

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(assignments);
      given(examRepository.findByLectureId(lectureId)).willReturn(exams);

      // when
      log.info("서비스 메서드 호출 - getLecutureStatistics");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      assertThat(response.getLectureId()).isEqualTo(lectureId);
      assertThat(response.getStudentCount()).isEqualTo(3);
      log.info("수강생 수: {}명", response.getStudentCount());
      
      assertThat(response.getAttendanceRate()).isCloseTo(80.0, org.assertj.core.data.Offset.offset(0.01));
      log.info("출석률: {}% (기대값: 80.0%) - 학생1: 80%, 학생2: 60%, 학생3: 100%의 평균", response.getAttendanceRate());
      
      assertThat(response.getAssignmentSubmissionRate()).isCloseTo(66.67, org.assertj.core.data.Offset.offset(0.01));
      log.info("과제 제출률: {}% (기대값: 66.67%) - 실제 제출: 4개 / 전체 가능: 6개 (3명 × 2과제)", response.getAssignmentSubmissionRate());
      
      assertThat(response.getExamParticipationRate()).isCloseTo(66.67, org.assertj.core.data.Offset.offset(0.01));
      log.info("시험 응시율: {}% (기대값: 66.67%) - 실제 응시: 4개 / 전체 가능: 6개 (3명 × 2시험)", response.getExamParticipationRate());
      
      assertThat(response.getAverageScore()).isCloseTo(81.25, org.assertj.core.data.Offset.offset(0.01));
      log.info("평균 점수: {}점 (기대값: 81.25점) - 시험1: 80점, 70점 / 시험2: 90점, 85점", response.getAverageScore());
      
      assertThat(response.getTotal10PercentScore()).isCloseTo(85.0, org.assertj.core.data.Offset.offset(0.01));
      log.info("상위 10% 평균 점수: {}점 (기대값: 85.0점) - 학생1 평균: 85점, 학생3 평균: 85점, 학생2 평균: 70점", response.getTotal10PercentScore());

      verify(lectureHelper).getLectureWithValidation(lectureId, teacherId);
      verify(lectureStudentRepository).findAllByLectureId(lectureId);
      verify(assignmentRepository).findByLectureId(lectureId);
      verify(examRepository).findByLectureId(lectureId);
      
      log.info("=== 모든 통계 계산 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 학생이 없을 때 모든 통계가 0으로 반환된다")
    void success_returnsZeroStatisticsWhenNoStudents() {
      log.info("=== 학생 없음 테스트 시작 ===");
      log.info("강의 ID: {}", lectureId);
      
      // given
      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());

      // when
      log.info("서비스 메서드 호출 - 학생이 없는 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      assertThat(response.getLectureId()).isEqualTo(lectureId);
      assertThat(response.getStudentCount()).isEqualTo(0);
      log.info("수강생 수: {}명", response.getStudentCount());
      
      assertThat(response.getAttendanceRate()).isEqualTo(0.0);
      assertThat(response.getAssignmentSubmissionRate()).isEqualTo(0.0);
      assertThat(response.getExamParticipationRate()).isEqualTo(0.0);
      assertThat(response.getAverageScore()).isEqualTo(0.0);
      assertThat(response.getTotal10PercentScore()).isEqualTo(0.0);
      
      log.info("모든 통계 값: 출석률={}%, 과제 제출률={}%, 시험 응시율={}%, 평균 점수={}점, 상위 10% 평균={}점",
          response.getAttendanceRate(),
          response.getAssignmentSubmissionRate(),
          response.getExamParticipationRate(),
          response.getAverageScore(),
          response.getTotal10PercentScore());
      
      log.info("=== 학생 없음 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 과제가 없을 때 과제 제출률은 0으로 반환된다")
    void success_returnsZeroAssignmentSubmissionRateWhenNoAssignments() {
      log.info("=== 과제 없음 테스트 시작 ===");
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1);

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());

      // 출석 관련: 빈 리스트 반환
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L), List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8),
              LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29))))
          .willReturn(Collections.emptyList());

      // when
      log.info("서비스 메서드 호출 - 과제가 없는 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      assertThat(response.getStudentCount()).isEqualTo(1);
      assertThat(response.getAssignmentSubmissionRate()).isEqualTo(0.0);
      assertThat(response.getExamParticipationRate()).isEqualTo(0.0);
      
      log.info("과제 제출률: {}% (기대값: 0.0%) - 과제가 없으므로 0 반환", response.getAssignmentSubmissionRate());
      log.info("=== 과제 없음 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 시험이 없을 때 시험 관련 통계는 0으로 반환된다")
    void success_returnsZeroExamStatisticsWhenNoExams() {
      log.info("=== 시험 없음 테스트 시작 ===");
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1);

      Assignment assignment1 = createAssignment(1L, lecture);
      List<Assignment> assignments = List.of(assignment1);

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(assignments);
      given(examRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());

      // 출석 관련: 빈 리스트 반환
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L), List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8),
              LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29))))
          .willReturn(Collections.emptyList());

      // 과제 제출 관련: 빈 리스트 반환
      given(assignmentSubmissionRepository.findAllByAssignmentIdsAndStudentIds(
          List.of(1L), List.of(1L)))
          .willReturn(Collections.emptyList());

      // when
      log.info("서비스 메서드 호출 - 시험이 없는 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      assertThat(response.getStudentCount()).isEqualTo(1);
      assertThat(response.getExamParticipationRate()).isEqualTo(0.0);
      assertThat(response.getAverageScore()).isEqualTo(0.0);
      assertThat(response.getTotal10PercentScore()).isEqualTo(0.0);
      
      log.info("시험 응시율: {}% (기대값: 0.0%)", response.getExamParticipationRate());
      log.info("평균 점수: {}점 (기대값: 0.0점)", response.getAverageScore());
      log.info("상위 10% 평균: {}점 (기대값: 0.0점)", response.getTotal10PercentScore());
      log.info("=== 시험 없음 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 출석 가능 날짜가 없을 때 출석률은 0으로 반환된다")
    void success_returnsZeroAttendanceRateWhenNoPossibleDates() {
      log.info("=== 출석 가능 날짜 없음 테스트 시작 ===");
      log.info("시나리오: 월요일 강의인데 기간이 화요일~일요일만 포함");
      
      // given
      // 강의 기간이 하루도 해당 요일이 없는 경우 (예: 월요일 강의인데 기간이 화요일~일요일만)
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 2, 9, 0)); // 화요일
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 2, 18, 0)); // 화요일

      // 출석 가능 날짜가 없으면 early return되므로 학생 관련 stubbing은 사용되지 않음
      User student1 = createStudent(1L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1);

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());

      // when
      log.info("서비스 메서드 호출 - 출석 가능 날짜가 없는 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      assertThat(response.getAttendanceRate()).isEqualTo(0.0);
      log.info("출석률: {}% (기대값: 0.0%) - 출석 가능 날짜가 없으므로 0 반환", response.getAttendanceRate());
      log.info("=== 출석 가능 날짜 없음 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 출석 상태가 false인 경우 출석으로 카운트되지 않는다")
    void success_excludesFalseAttendanceStatus() {
      log.info("=== 출석 상태 false 테스트 시작 ===");
      log.info("시나리오: 출석 기록은 있지만 status가 false인 경우는 출석으로 카운트되지 않음");
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1);

      // 출석 기록은 있지만 status가 false인 경우 (Repository에서 status=true만 조회하므로 빈 리스트 반환)
      List<LocalDate> attendanceDates = List.of(
          LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15),
          LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)
      );
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L), attendanceDates))
          .willReturn(Collections.emptyList());

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());

      // when
      log.info("서비스 메서드 호출 - 출석 상태가 false인 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      // 5일 중 0일 출석 = 0%
      assertThat(response.getAttendanceRate()).isEqualTo(0.0);
      log.info("출석률: {}% (기대값: 0.0%) - status가 false인 출석 기록은 제외됨", response.getAttendanceRate());
      log.info("=== 출석 상태 false 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: earnedScore가 null인 시험 제출은 평균 점수 계산에서 제외된다")
    void success_excludesNullScoresFromAverage() {
      log.info("=== null 점수 제외 테스트 시작 ===");
      log.info("시나리오: earnedScore가 null인 제출은 평균 점수 계산에서 제외");
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1);

      Exam exam1 = createExam(1L, lecture);
      List<Exam> exams = List.of(exam1);

      // earnedScore가 null인 제출 (calculateAverageScore에서는 getEarnedScore()만 사용)
      ExamStudent examStudentWithNullScore = createExamStudentForAverage(exam1, student1, null);
      ExamStudent examStudentWithScore = createExamStudentForAverage(exam1, student1, 80);

      given(examStudentRepository.findAllByExamIds(List.of(1L)))
          .willReturn(List.of(examStudentWithNullScore, examStudentWithScore));

      // 출석 관련: 빈 리스트 반환
      List<LocalDate> attendanceDates = List.of(
          LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15),
          LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)
      );
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L), attendanceDates))
          .willReturn(Collections.emptyList());

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(exams);

      // when
      log.info("서비스 메서드 호출 - null 점수가 있는 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      // null 점수는 제외되고 80점만 계산됨
      assertThat(response.getAverageScore()).isEqualTo(80.0);
      log.info("평균 점수: {}점 (기대값: 80.0점) - null 점수는 제외되고 80점만 계산됨", response.getAverageScore());
      log.info("=== null 점수 제외 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 상위 10% 계산 시 학생 수가 적을 때 최소 1명을 선택한다")
    void success_selectsAtLeastOneStudentForTop10Percent() {
      log.info("=== 상위 10% 최소 1명 선택 테스트 시작 ===");
      log.info("시나리오: 학생 2명일 때 상위 10% = Math.ceil(2 * 0.1) = 1명 선택");
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      User student2 = createStudent(2L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      LectureStudent lectureStudent2 = createLectureStudent(lecture, student2);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1, lectureStudent2);

      Exam exam1 = createExam(1L, lecture);
      List<Exam> exams = List.of(exam1);

      // 학생1: 100점, 학생2: 50점 (calculateTop10PercentAverage에서는 getExam(), getStudent() 사용)
      ExamStudent exam1Student1 = createExamStudent(exam1, student1, 100);
      ExamStudent exam1Student2 = createExamStudent(exam1, student2, 50);

      given(examStudentRepository.findAllByExamIds(List.of(1L)))
          .willReturn(List.of(exam1Student1, exam1Student2));

      given(examStudentRepository.findAllByExamIdsAndStudentIds(
          List.of(1L), List.of(1L, 2L)))
          .willReturn(List.of(exam1Student1, exam1Student2));

      // 출석 관련: 빈 리스트 반환
      List<LocalDate> attendanceDates = List.of(
          LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15),
          LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)
      );
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L, 2L), attendanceDates))
          .willReturn(Collections.emptyList());

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(exams);

      // when
      log.info("서비스 메서드 호출 - 학생1: 100점, 학생2: 50점");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      // 상위 10% = Math.ceil(2 * 0.1) = 1명
      // 상위 1명의 평균 = 100점
      assertThat(response.getTotal10PercentScore()).isEqualTo(100.0);
      log.info("상위 10% 평균 점수: {}점 (기대값: 100.0점) - 상위 1명(학생1)의 평균", response.getTotal10PercentScore());
      log.info("=== 상위 10% 최소 1명 선택 테스트 완료 ===");
    }

    @Test
    @DisplayName("성공: 시험 점수가 없는 학생은 상위 10% 계산에서 제외된다")
    void success_excludesStudentsWithoutScoresFromTop10Percent() {
      log.info("=== 점수 없는 학생 제외 테스트 시작 ===");
      log.info("시나리오: 학생1: 100점, 학생2: 80점, 학생3: 점수 없음 → 학생3은 제외");
      
      // given
      given(lecture.getDaysOfWeek()).willReturn(List.of(DayOfWeek.MONDAY));
      given(lecture.getStartDate()).willReturn(LocalDateTime.of(2024, 1, 1, 9, 0));
      given(lecture.getEndDate()).willReturn(LocalDateTime.of(2024, 1, 29, 18, 0));

      User student1 = createStudent(1L);
      User student2 = createStudent(2L);
      User student3 = createStudent(3L);
      LectureStudent lectureStudent1 = createLectureStudent(lecture, student1);
      LectureStudent lectureStudent2 = createLectureStudent(lecture, student2);
      LectureStudent lectureStudent3 = createLectureStudent(lecture, student3);
      List<LectureStudent> lectureStudents = List.of(lectureStudent1, lectureStudent2, lectureStudent3);

      Exam exam1 = createExam(1L, lecture);
      List<Exam> exams = List.of(exam1);

      // 학생1: 100점, 학생2: 80점, 학생3: 점수 없음 (calculateTop10PercentAverage에서는 getExam(), getStudent() 사용)
      ExamStudent exam1Student1 = createExamStudent(exam1, student1, 100);
      ExamStudent exam1Student2 = createExamStudent(exam1, student2, 80);

      given(examStudentRepository.findAllByExamIds(List.of(1L)))
          .willReturn(List.of(exam1Student1, exam1Student2));

      given(examStudentRepository.findAllByExamIdsAndStudentIds(
          List.of(1L), List.of(1L, 2L, 3L)))
          .willReturn(List.of(exam1Student1, exam1Student2));

      // 출석 관련: 빈 리스트 반환
      List<LocalDate> attendanceDates = List.of(
          LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15),
          LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)
      );
      given(studentAttendanceRepository.findAllByStudentIdsAndDates(
          List.of(1L, 2L, 3L), attendanceDates))
          .willReturn(Collections.emptyList());

      given(lectureHelper.getLectureWithValidation(lectureId, teacherId)).willReturn(lecture);
      given(lectureStudentRepository.findAllByLectureId(lectureId)).willReturn(lectureStudents);
      given(assignmentRepository.findByLectureId(lectureId)).willReturn(Collections.emptyList());
      given(examRepository.findByLectureId(lectureId)).willReturn(exams);

      // when
      log.info("서비스 메서드 호출 - 점수가 없는 학생이 있는 경우");
      LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

      // then
      log.info("=== 계산 결과 검증 ===");
      // 학생1 평균: 100, 학생2 평균: 80
      // 상위 10% = Math.ceil(2 * 0.1) = 1명
      // 상위 1명의 평균 = 100점
      assertThat(response.getTotal10PercentScore()).isEqualTo(100.0);
      log.info("상위 10% 평균 점수: {}점 (기대값: 100.0점) - 점수 없는 학생3은 제외되고 상위 1명(학생1)의 평균", response.getTotal10PercentScore());
      log.info("=== 점수 없는 학생 제외 테스트 완료 ===");
    }
  }

  // Helper methods
  private User createStudent(Long studentId) {
    User student = mock(User.class);
    // 일부 테스트에서 사용되지 않을 수 있으므로 lenient로 처리
    lenient().when(student.getId()).thenReturn(studentId);
    return student;
  }

  private LectureStudent createLectureStudent(Lecture lecture, User student) {
    LectureStudent lectureStudent = mock(LectureStudent.class);
    // 일부 테스트에서 사용되지 않을 수 있으므로 lenient로 처리
    lenient().when(lectureStudent.getStudent()).thenReturn(student);
    return lectureStudent;
  }

  private Assignment createAssignment(Long assignmentId, Lecture lecture) {
    Assignment assignment = mock(Assignment.class);
    given(assignment.getId()).willReturn(assignmentId);
    // getLecture()는 실제로 사용되지 않으므로 lenient로 처리
    lenient().when(assignment.getLecture()).thenReturn(lecture);
    return assignment;
  }

  private Exam createExam(Long examId, Lecture lecture) {
    Exam exam = mock(Exam.class);
    given(exam.getId()).willReturn(examId);
    // getLecture()는 실제로 사용되지 않으므로 lenient로 처리
    lenient().when(exam.getLecture()).thenReturn(lecture);
    return exam;
  }

  private Attendance createAttendance(User student, LocalDate date, boolean status) {
    Attendance attendance = mock(Attendance.class);
    given(attendance.getStudent()).willReturn(student);
    given(attendance.getDate()).willReturn(date);
    // findAllByStudentIdsAndDates는 이미 status=true만 조회하므로 isStatus()는 사용되지 않음
    // 하지만 테스트 가독성을 위해 유지 (lenient로 처리하지 않음 - 실제로 사용되지 않으므로)
    lenient().when(attendance.isStatus()).thenReturn(status);
    return attendance;
  }

  private AssignmentSubmission createAssignmentSubmission(Assignment assignment, User student) {
    AssignmentSubmission submission = mock(AssignmentSubmission.class);
    given(submission.getAssignment()).willReturn(assignment);
    given(submission.getStudent()).willReturn(student);
    return submission;
  }

  private ExamStudent createExamStudent(Exam exam, User student, Integer earnedScore) {
    ExamStudent examStudent = mock(ExamStudent.class);
    given(examStudent.getExam()).willReturn(exam);
    given(examStudent.getStudent()).willReturn(student);
    given(examStudent.getEarnedScore()).willReturn(earnedScore);
    return examStudent;
  }

  // calculateAverageScore 전용: getEarnedScore()만 사용하므로 getExam(), getStudent() stubbing 불필요
  private ExamStudent createExamStudentForAverage(Exam exam, User student, Integer earnedScore) {
    ExamStudent examStudent = mock(ExamStudent.class);
    given(examStudent.getEarnedScore()).willReturn(earnedScore);
    return examStudent;
  }
}
