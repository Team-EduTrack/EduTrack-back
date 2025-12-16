package com.edutrack.domain.student.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학생 대시보드 서비스
 * - 강의, 과제, 시험 목록 조회
 * - 출석 체크
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentLectureQueryRepository lectureQueryRepository;
    private final StudentAssignmentQueryRepository assignmentQueryRepository;
    private final StudentExamQueryRepository examQueryRepository;
    private final StudentAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

  private final LectureStudentRepository lectureStudentRepository;
  private final LectureRepository lectureRepository;
  private final AssignmentRepository assignmentRepository;
  private final ExamRepository examRepository;
  private final ExamStudentRepository examStudentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;

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
   * 내 강의 상세 조회
   */
  @Transactional(readOnly = true)
  public MyLectureDetailResponse getMyLectureDetail(Long studentId, Long lectureId) {
    // 1. 학생 존재 확인
    validateStudent(studentId);

    // 2. 강의 조회 및 수강 여부 확인
    Lecture lecture = lectureRepository.findById(lectureId)
        .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));

    boolean isEnrolled = lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId);
    if (!isEnrolled) {
      throw new ForbiddenException("해당 강의를 수강 중인 학생만 조회할 수 있습니다.");
    }

    // 3. 출석률 계산
    Double attendanceRate = calculateAttendanceRate(studentId, lecture);

    // 4. 과제 제출률 계산 (학생이 제출한 과제수 / 강의의 과제수)
    Double assignmentSubmissionRate = calculateAssignmentSubmissionRate(studentId, lectureId);

    // 5. 시험 목록 조회 (내가 봐야 하는 시험만 - 이미 본 시험 제외)
    List<MyLectureDetailResponse.ExamInfo> exams = getExamListForStudent(studentId, lectureId);

    // 6. 과제 목록 조회 (내가 제출해야 하는 과제만 - 이미 제출한 과제 제외)
    List<MyLectureDetailResponse.AssignmentInfo> assignments = getAssignmentListForStudent(studentId, lectureId);

    // 7. DTO 생성 및 반환
    return MyLectureDetailResponse.builder()
        .lectureId(lecture.getId())
        .lectureTitle(lecture.getTitle())
        .teacherName(lecture.getTeacher().getName())
        .description(lecture.getDescription())
        .attendanceRate(attendanceRate)
        .assignmentSubmissionRate(assignmentSubmissionRate)
        .exams(exams)
        .assignments(assignments)
        .build();
  }

  /**
   * 출석률 계산
   */
  private Double calculateAttendanceRate(Long studentId, Lecture lecture) {
    LocalDate startDate = lecture.getStartDate().toLocalDate();
    LocalDate endDate = lecture.getEndDate().toLocalDate();

    // 강의 기간 동안 출석한 기록 조회
    List<Attendance> attendances = attendanceRepository
        .findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
            studentId, startDate, endDate
        );

    int attendedCount = attendances.size();

    // 강의 요일 기반 실제 수업일 수 계산
    int totalClassDays = calculateTotalClassDays(lecture, startDate, endDate);

    if (totalClassDays == 0) {
      return 0.0;
    }

    // 백분율 계산 (0.0 ~ 100.0) - 소수점 첫째 자리까지 반올림
    double rate = (double) attendedCount / totalClassDays * 100.0;
    return Math.round(rate * 10.0) / 10.0;
  }

  /**
   * 강의 요일 기반 실제 수업일 수 계산
   */
  private int calculateTotalClassDays(Lecture lecture, LocalDate startDate, LocalDate endDate) {
    List<DayOfWeek> daysOfWeek = lecture.getDaysOfWeek();
    int count = 0;

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      if (daysOfWeek.contains(current.getDayOfWeek())) {
        count++;
      }
      current = current.plusDays(1);
    }

    return count;
  }

  /**
   * 과제 제출률 계산 (학생이 제출한 과제수 / 강의의 과제수, Double 백분율 값 반환 - 소수점 첫째 자리까지)
   */
  private Double calculateAssignmentSubmissionRate(Long studentId, Long lectureId) {
    // 전체 과제 수 (기존 메서드 사용)
    List<Assignment> allAssignments = assignmentRepository.findByLectureId(lectureId);
    int totalCount = allAssignments.size();

    if (totalCount == 0) {
      return 0.0;
    }

    // 제출한 과제 수
    int submittedCount = (int) allAssignments.stream()
        .filter(assignment ->
            assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(
                assignment.getId(), studentId
            )
        )
        .count();

    // 백분율 계산 (0.0 ~ 100.0) - 소수점 첫째 자리까지 반올림
    double rate = (double) submittedCount / totalCount * 100.0;
    return Math.round(rate * 10.0) / 10.0;
  }

  /**
   * 학생이 봐야 하는 시험 목록 조회 (이미 본 시험은 제외)
   */
  private List<MyLectureDetailResponse.ExamInfo> getExamListForStudent(
      Long studentId, Long lectureId
  ) {
    // 1. 강의의 모든 시험 조회
    List<Exam> allExams = examRepository.findByLectureId(lectureId);

    if (allExams.isEmpty()) {
      return List.of();
    }

    // 2. 시험 ID 목록 추출
    List<Long> examIds = allExams.stream()
        .map(Exam::getId)
        .toList();

    // 3. 해당 학생의 시험 응시 기록을 한 번에 조회
    List<ExamStudent> examStudents = examStudentRepository
        .findAllByExamIdsAndStudentIds(examIds, List.of(studentId));

    // 4. 이미 본 시험 ID Set 생성 (ExamStudent가 존재하면 이미 본 시험)
    java.util.Set<Long> completedExamIds = examStudents.stream()
        .map(es -> es.getExam().getId())
        .collect(Collectors.toSet());

    // 5. 아직 보지 않은 시험만 필터링하여 DTO 생성
    return allExams.stream()
        .filter(exam -> !completedExamIds.contains(exam.getId())) // 이미 본 시험 제외
        .sorted(Comparator.comparing(Exam::getStartDate)) // 시작일 순 정렬
        .map(exam -> MyLectureDetailResponse.ExamInfo.builder()
            .examId(exam.getId())
            .examTitle(exam.getTitle())
            .startDate(exam.getStartDate())
            .endDate(exam.getEndDate())
            .build())
        .toList();
  }

  /**
   * 학생이 제출해야 하는 과제 목록 조회 (이미 제출한 과제는 제외)
   */
  private List<MyLectureDetailResponse.AssignmentInfo> getAssignmentListForStudent(
      Long studentId, Long lectureId
  ) {
    // 강의의 모든 과제 조회
    List<Assignment> allAssignments = assignmentRepository.findByLectureId(lectureId);

    // 이미 제출한 과제는 제외하고, 아직 제출하지 않은 과제만 필터링
    return allAssignments.stream()
        .filter(assignment ->
            !assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(
                assignment.getId(), studentId
            )
        )
        .map(assignment -> MyLectureDetailResponse.AssignmentInfo.builder()
            .assignmentId(assignment.getId())
            .assignmentTitle(assignment.getTitle())
            .startDate(assignment.getStartDate())
            .endDate(assignment.getEndDate())
            .build())
        .toList();
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
