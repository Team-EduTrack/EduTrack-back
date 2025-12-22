package com.edutrack.domain.lecture.dto;

import java.util.List;

import com.edutrack.domain.lecture.entity.LectureStudent;

import lombok.Getter;


/**
 * LectureForTeacherResponse와 겹치는 부분이 많지만, 추후에 학생 리스트, 과제 제출, 통계등 상세정보를 표시해야하니
 * 해당 DTO는 상위 DTO로 인지하고 계시면 될 것 같습니다.
 */
@Getter
public class LectureDetailForTeacherResponse extends LectureForTeacherResponse{
  private final String description;

  // 강의를 듣는 학생 리스트
  private final List<StudentInfo> studentDetails;

  // 과제별 제출 학생 목록 (제출자가 있는 과제만 포함)
  private final List<AssignmentWithSubmissions> assignmentsWithSubmissions;

  // 시험별 응시 현황 목록
  private final List<ExamParticipationInfo> examsWithParticipation;

  /**
   * LectureStudent 리스트 기반으로 학생 정보 매핑
   */
  public LectureDetailForTeacherResponse(Long lectureId, String title, String description, List<LectureStudent> lectureStudents, String teacherName, Double averageGrade, String imageUrl, List<AssignmentWithSubmissions> assignmentsWithSubmissions, List<ExamParticipationInfo> examsWithParticipation) {
    super(lectureId, title, lectureStudents.size(), teacherName, averageGrade, imageUrl);
    this.description = description;

    // LectureStudent에서 학생(User) 추출 후 DTO로 변환
    this.studentDetails = lectureStudents.stream()
        .map(LectureStudent::getStudent)
        .map(user -> new StudentInfo(user.getId(), user.getName()))
        .toList();
    
    this.assignmentsWithSubmissions = assignmentsWithSubmissions != null ? assignmentsWithSubmissions : List.of();
    this.examsWithParticipation = examsWithParticipation != null ? examsWithParticipation : List.of();
  }

  // 내부 정적 클래스: 학생 정보
  @Getter
  public static class StudentInfo {
    private final Long id;
    private final String name;

    public StudentInfo(Long id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  // 내부 정적 클래스: 과제와 제출 학생 정보
  @Getter
  public static class AssignmentWithSubmissions {
    private final Long assignmentId;
    private final String assignmentTitle;
    private final List<SubmissionStudentInfo> submittedStudents;

    public AssignmentWithSubmissions(Long assignmentId, String assignmentTitle, List<SubmissionStudentInfo> submittedStudents) {
      this.assignmentId = assignmentId;
      this.assignmentTitle = assignmentTitle;
      this.submittedStudents = submittedStudents;
    }
  }

  // 내부 정적 클래스: 과제 제출 학생 정보
  @Getter
  public static class SubmissionStudentInfo {
    private final Long studentId;
    private final String studentName;
    private final Long submissionId;

    public SubmissionStudentInfo(Long studentId, String studentName, Long submissionId) {
      this.studentId = studentId;
      this.studentName = studentName;
      this.submissionId = submissionId;
    }
  }

  @Getter
  public static class ExamParticipationInfo {
    private final Long examId;
    private final String examTitle;
    private final int participatedCount;    // 응시한 학생 수
    private final int totalStudentCount;    // 강의에 할당된 전체 학생 수

    public ExamParticipationInfo(Long examId, String examTitle, int participatedCount, int totalStudentCount) {
      this.examId = examId;
      this.examTitle = examTitle;
      this.participatedCount = participatedCount;
      this.totalStudentCount = totalStudentCount;
    }
  }
}