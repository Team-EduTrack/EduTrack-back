package com.edutrack.domain.statistics.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.api.student.repository.StudentAttendanceRepository;
import com.edutrack.domain.assignment.entity.Assignment;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureStatisticsService {

  private final LectureStudentRepository lectureStudentRepository;
  private final StudentAttendanceRepository studentAttendanceRepository;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final ExamRepository examRepository;
  private final ExamStudentRepository examStudentRepository;
  private final LectureHelper lectureHelper;


  @Transactional(readOnly = true)
  public LectureStatisticsResponse getLecutureStatistics(Long lectureId, Long teacherId) {

    Lecture lecture = lectureHelper.getLectureOrThrow(lectureId);

    User teacher = lectureHelper.getTeacherOrThrow(teacherId);

    lectureHelper.validateLectureAcess(lectureId, teacher, lecture);

    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);
    List<Assignment> assignments = assignmentRepository.findByLectureId(lectureId);
    List<Exam> exams = examRepository.findByLectureId(lectureId);

    int studentCount = lectureStudents.size();

    Double attendanceRate = calculateAttendanceRate(lecture, lectureStudents);
    Double assignmentSubmissionRate = calculateAssignmentSubmissionRate(lectureStudents, assignments);
    Double examParticipationRate = calculateExamParticipationRate(lectureStudents, exams);
    Double averageScore = calculateAverageScore(exams);
    Double top10PercentAverage = calculateTop10PercentAverage(lectureStudents, exams);

    return  new LectureStatisticsResponse(
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

    double totalAttendanceRate = 0.0;
    for (LectureStudent lectureStudent : lectureStudents) {
      Long studentId = lectureStudent.getStudent().getId();
      
      long actualAttendanceCount = attendancePossibleDates.stream()
          .filter(date -> {
            Optional<Attendance> attendance = studentAttendanceRepository.findByStudentIdAndDate(studentId, date);
            return attendance.isPresent() && attendance.get().isStatus();
          })
          .count();

      double studentAttendanceRate = (double) actualAttendanceCount / possibleAttendanceCount * 100.0;
      totalAttendanceRate += studentAttendanceRate;
    }

    return totalAttendanceRate / lectureStudents.size();
  }

  /**
   * 강의 기간 내 출석 가능한 날짜 계산 (해당 요일만)
   * 예: 강의가 월요일이면, 강의 기간 내 모든 월요일 반환
   */
  private List<LocalDate> calculateAttendancePossibleDates(Lecture lecture) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate startDate = lecture.getStartDate().toLocalDate();
    LocalDate endDate = lecture.getEndDate().toLocalDate();
    DayOfWeek lectureDayOfWeek = lecture.getDayOfWeek();

    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      if (currentDate.getDayOfWeek() == lectureDayOfWeek) {
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

    long totalActualSubmissions = 0;
    for (LectureStudent lectureStudent : lectureStudents) {
      Long studentId = lectureStudent.getStudent().getId();
      for (Assignment assignment : assignments) {
        if (assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(assignment.getId(), studentId)) {
          totalActualSubmissions++;
        }
      }
    }

    if (totalPossibleSubmissions == 0) {
      return 0.0;
    }

    return (double) totalActualSubmissions / totalPossibleSubmissions * 100.0;
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

    long totalActualParticipations = 0;
    for (LectureStudent lectureStudent : lectureStudents) {
      Long studentId = lectureStudent.getStudent().getId();
      for (Exam exam : exams) {
        if (examStudentRepository.existsByExamIdAndStudentId(exam.getId(), studentId)) {
          totalActualParticipations++;
        }
      }
    }

    if (totalPossibleParticipations == 0) {
      return 0.0;
    }

    return (double) totalActualParticipations / totalPossibleParticipations * 100.0;
  }

  /**
   * 평균 점수 계산
   * 강의의 모든 시험에서 채점 완료된 점수들의 평균
   */
  private Double calculateAverageScore(List<Exam> exams) {
    if (exams.isEmpty()) {
      return 0.0;
    }

    List<Integer> allScores = new ArrayList<>();
    for (Exam exam : exams) {
      List<ExamStudent> examStudents = examStudentRepository.findByExamId(exam.getId());
      
      for (ExamStudent examStudent : examStudents) {
        if (examStudent.getEarnedScore() != null) {
          allScores.add(examStudent.getEarnedScore());
        }
      }
    }

    if (allScores.isEmpty()) {
      return 0.0;
    }

    double sum = allScores.stream()
        .mapToInt(Integer::intValue)
        .sum();

    return sum / allScores.size();
  }

  /**
   * 상위 10% 평균 점수 계산
   * 각 학생의 평균 점수를 계산하고, 상위 10% 학생들의 평균 점수 평균
   */
  private Double calculateTop10PercentAverage(List<LectureStudent> lectureStudents, List<Exam> exams) {
    if (lectureStudents.isEmpty() || exams.isEmpty()) {
      return 0.0;
    }

    List<Double> studentAverages = new ArrayList<>();
    for (LectureStudent lectureStudent : lectureStudents) {
      Long studentId = lectureStudent.getStudent().getId();
      
      List<Integer> studentScores = new ArrayList<>();
      for (Exam exam : exams) {
        examStudentRepository.findByExamIdAndStudentId(exam.getId(), studentId)
            .ifPresent(examStudent -> {
              if (examStudent.getEarnedScore() != null) {
                studentScores.add(examStudent.getEarnedScore());
              }
            });
      }

      if (!studentScores.isEmpty()) {
        double studentAverage = studentScores.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        studentAverages.add(studentAverage);
      }
    }

    if (studentAverages.isEmpty()) {
      return 0.0;
    }

    List<Double> sortedAverages = studentAverages.stream()
        .sorted((a, b) -> Double.compare(b, a))
        .toList();

    int top10PercentCount = Math.max(1, (int) Math.ceil(sortedAverages.size() * 0.1));

    double top10PercentSum = sortedAverages.stream()
        .limit(top10PercentCount)
        .mapToDouble(Double::doubleValue)
        .sum();

    return top10PercentSum / top10PercentCount;
  }
}
