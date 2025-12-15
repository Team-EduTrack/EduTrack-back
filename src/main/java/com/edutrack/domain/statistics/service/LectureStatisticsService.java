package com.edutrack.domain.statistics.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.api.student.repository.StudentAttendanceRepository;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureStatisticsService {

  private static final double PERCENTAGE_MULTIPLIER = 100.0;
  private static final double TOP_PERCENTAGE_RATIO = 0.1;
  private static final int MIN_TOP_STUDENT_COUNT = 1;

  private final LectureStudentRepository lectureStudentRepository;
  private final StudentAttendanceRepository studentAttendanceRepository;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final ExamRepository examRepository;
  private final ExamStudentRepository examStudentRepository;
  private final LectureHelper lectureHelper;


  @Transactional(readOnly = true)
  public LectureStatisticsResponse getLecutureStatistics(Long lectureId, Long teacherId) {

    Lecture lecture = lectureHelper.getLectureWithValidation(lectureId, teacherId);

    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);
    List<Assignment> assignments = assignmentRepository.findByLectureId(lectureId);
    List<Exam> exams = examRepository.findByLectureId(lectureId);

    int studentCount = lectureStudents.size();

    Double attendanceRate = calculateAttendanceRate(lecture, lectureStudents);
    Double assignmentSubmissionRate = calculateAssignmentSubmissionRate(lectureStudents, assignments);
    Double examParticipationRate = calculateExamParticipationRate(lectureStudents, exams);
    Double averageScore = calculateAverageScore(exams);
    Double top10PercentAverage = calculateTop10PercentAverage(lectureStudents, exams);

    return new LectureStatisticsResponse(
        lectureId,
        studentCount,
        attendanceRate,
        assignmentSubmissionRate,
        examParticipationRate,
        averageScore,
        top10PercentAverage
    );
  }

  /**
   * 평균 출석률 = 모든 학생의 출석률 평균
   */
  private Double calculateAttendanceRate(Lecture lecture, List<LectureStudent> lectureStudents) {
    if (lectureStudents.isEmpty()) {
      return 0.0;
    }
  
    List<LocalDate> attendancePossibleDates = calculateAttendancePossibleDates(lecture);
    if (attendancePossibleDates.isEmpty()) {
      return 0.0;
    }
  
    int possibleAttendanceCount = attendancePossibleDates.size();
  
    // 1. 학생 ID 목록 수집
    List<Long> studentIds = lectureStudents.stream()
        .map(ls -> ls.getStudent().getId())
        .toList();
  
    // 2. 한 번의 쿼리로 모든 출석 데이터 조회
    List<Attendance> allAttendances = studentAttendanceRepository
        .findAllByStudentIdsAndDates(studentIds, attendancePossibleDates);
  
    // 3. (studentId, date) 조합을 키로 하는 Set 생성
    Set<String> attendanceKeys = allAttendances.stream()
        .map(a -> a.getStudent().getId() + "_" + a.getDate())
        .collect(Collectors.toSet());
  
    // 4. 각 학생의 출석률 계산
    double totalAttendanceRate = 0.0;
    for (LectureStudent lectureStudent : lectureStudents) {
      Long studentId = lectureStudent.getStudent().getId();
      
      long actualAttendanceCount = attendancePossibleDates.stream()
          .filter(date -> attendanceKeys.contains(studentId + "_" + date))
          .count();
  
      double studentAttendanceRate = (double) actualAttendanceCount / possibleAttendanceCount * PERCENTAGE_MULTIPLIER;
      totalAttendanceRate += studentAttendanceRate;
    }
  
    return totalAttendanceRate / lectureStudents.size();
  }

  /**
   * 강의 기간 내 출석 가능한 날짜 계산 (해당 요일들만)
   * 예: 강의가 월요일, 수요일이면, 강의 기간 내 모든 월요일과 수요일 반환
   */
  private List<LocalDate> calculateAttendancePossibleDates(Lecture lecture) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate startDate = lecture.getStartDate().toLocalDate();
    LocalDate endDate = lecture.getEndDate().toLocalDate();
    List<DayOfWeek> lectureDaysOfWeek = lecture.getDaysOfWeek();

    if (lectureDaysOfWeek.isEmpty()) {
      return dates;
    }

    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      if (lectureDaysOfWeek.contains(currentDate.getDayOfWeek())) {
        dates.add(currentDate);
      }
      currentDate = currentDate.plusDays(1);
    }

    return dates;
  }

  /**
   * 과제 제출률 계산
   * 제출률 = (학생들이 제출한 과제 개수 / 전체 과제 개수) * 100
   * 전체 과제 개수 = 학생 수 × 과제 수
   */
  private Double calculateAssignmentSubmissionRate(List<LectureStudent> lectureStudents, List<Assignment> assignments) {
    if (lectureStudents.isEmpty() || assignments.isEmpty()) {
      return 0.0;
    }

    long totalPossibleSubmissions = (long) lectureStudents.size() * assignments.size();

    // 1. ID 목록 수집
    List<Long> assignmentIds = assignments.stream()
        .map(Assignment::getId)
        .toList();

    List<Long> studentIds = lectureStudents.stream()
        .map(ls -> ls.getStudent().getId())
        .toList();

    // 2. 한 번의 쿼리로 모든 제출 데이터 조회
    List<AssignmentSubmission> submissions = assignmentSubmissionRepository
        .findAllByAssignmentIdsAndStudentIds(assignmentIds, studentIds);

    // 3. Set으로 변환
    Set<String> submissionKeys = submissions.stream()
        .map(s -> s.getAssignment().getId() + "_" + s.getStudent().getId())
        .collect(Collectors.toSet());

    // 4. Stream으로 제출 개수 계산
    long totalActualSubmissions = lectureStudents.stream()
        .flatMap(ls -> assignments.stream()
            .map(a -> a.getId() + "_" + ls.getStudent().getId()))
        .filter(submissionKeys::contains)
        .count();

    return (double) totalActualSubmissions / totalPossibleSubmissions * PERCENTAGE_MULTIPLIER;
  }

  /**
   * 시험 응시율 계산
   * 응시율 = (학생들이 응시한 시험 개수 / 전체 시험 개수) * 100
   * 전체 시험 개수 = 학생 수 × 시험 수
   */
  private Double calculateExamParticipationRate(List<LectureStudent> lectureStudents, List<Exam> exams) {
    if (lectureStudents.isEmpty() || exams.isEmpty()) {
      return 0.0;
    }

    long totalPossibleParticipations = (long) lectureStudents.size() * exams.size();

    // 1. ID 목록 수집
    List<Long> examIds = exams.stream()
        .map(Exam::getId)
        .toList();

    List<Long> studentIds = lectureStudents.stream()
        .map(ls -> ls.getStudent().getId())
        .toList();

    // 2. 한 번의 쿼리로 모든 응시 데이터 조회
    List<ExamStudent> examStudents = examStudentRepository
        .findAllByExamIdsAndStudentIds(examIds, studentIds);

    // 3. Set으로 변환
    Set<String> participationKeys = examStudents.stream()
        .map(es -> es.getExam().getId() + "_" + es.getStudent().getId())
        .collect(Collectors.toSet());

    // 4. Stream으로 응시 개수 계산
    long totalActualParticipations = lectureStudents.stream()
        .flatMap(ls -> exams.stream()
            .map(e -> e.getId() + "_" + ls.getStudent().getId()))
        .filter(participationKeys::contains)
        .count();

    if (totalPossibleParticipations == 0) {
      return 0.0;
    }

    return (double) totalActualParticipations / totalPossibleParticipations * PERCENTAGE_MULTIPLIER;
  }

  /**
   * 평균 점수 계산
   * 강의의 모든 시험에서 채점 완료된 점수들의 평균
   * LectureService에서 재사용됨
   */
  public Double calculateAverageScore(List<Exam> exams) {
    if (exams.isEmpty()) {
      return 0.0;
    }

    // 1. 시험 ID 목록 수집
    List<Long> examIds = exams.stream()
        .map(Exam::getId)
        .toList();

    // 2. 한 번의 쿼리로 모든 시험의 응시 기록 조회
    List<ExamStudent> allExamStudents = examStudentRepository.findAllByExamIds(examIds);

    // 3. 채점 완료된 점수의 평균 계산 (Stream 1번만 사용)
    return allExamStudents.stream()
        .filter(es -> es.getEarnedScore() != null)
        .mapToInt(ExamStudent::getEarnedScore)
        .average()
        .orElse(0.0);
  }

  /**
   * 상위 10% 평균 점수 계산
   * 각 학생의 평균 점수를 계산하고, 상위 10% 학생들의 평균 점수 평균
   */
  private Double calculateTop10PercentAverage(List<LectureStudent> lectureStudents, List<Exam> exams) {
    if (lectureStudents.isEmpty() || exams.isEmpty()) {
      return 0.0;
    }

    // 1. ID 목록 수집
    List<Long> examIds = exams.stream().map(Exam::getId).toList();
    List<Long> studentIds = lectureStudents.stream()
        .map(ls -> ls.getStudent().getId())
        .toList();

    // 2. 한 번의 쿼리로 모든 응시 기록 조회
    List<ExamStudent> allExamStudents = examStudentRepository
        .findAllByExamIdsAndStudentIds(examIds, studentIds);

    // 3. (examId, studentId) 조합을 키로 하는 Map 생성
    Map<String, ExamStudent> examStudentMap = allExamStudents.stream()
        .collect(Collectors.toMap(
            es -> es.getExam().getId() + "_" + es.getStudent().getId(),
            es -> es
        ));

    // 4. 각 학생의 평균 점수 계산
    List<Double> studentAverages = lectureStudents.stream()
        .map(ls -> {
          Long studentId = ls.getStudent().getId();
          List<Integer> studentScores = exams.stream()
              .map(exam -> {
                String key = exam.getId() + "_" + studentId;
                ExamStudent examStudent = examStudentMap.get(key);
                return (examStudent != null && examStudent.getEarnedScore() != null)
                    ? examStudent.getEarnedScore()
                    : null;
              })
              .filter(score -> score != null)
              .toList();

          if (studentScores.isEmpty()) {
            return null;
          }

          return studentScores.stream()
              .mapToInt(Integer::intValue)
              .average()
              .orElse(0.0);
        })
        .filter(avg -> avg != null)
        .toList();

    if (studentAverages.isEmpty()) {
      return 0.0;
    }

    // 5. 상위 10% 계산
    List<Double> sortedAverages = studentAverages.stream()
        .sorted((a, b) -> Double.compare(b, a))
        .toList();

    int top10PercentCount = Math.max(MIN_TOP_STUDENT_COUNT, (int) Math.ceil(sortedAverages.size() * TOP_PERCENTAGE_RATIO));

    double top10PercentSum = sortedAverages.stream()
        .limit(top10PercentCount)
        .mapToDouble(Double::doubleValue)
        .sum();

    return top10PercentSum / top10PercentCount;
  }
}
