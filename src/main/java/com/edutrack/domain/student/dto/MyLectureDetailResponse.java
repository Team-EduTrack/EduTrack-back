package com.edutrack.domain.student.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.edutrack.domain.assignment.dto.AssignmentSubmissionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyLectureDetailResponse {

  private final Long lectureId;
  private final String lectureTitle;
  private final String teacherName;
  private final String description;

  // 출석률
  private final Double attendanceRate;

  // 과제 제출률
  private final Double assignmentSubmissionRate;

  // 시험 목록
  private final List<ExamInfo> exams;

  // 과제 목록
  private final List<AssignmentInfo> assignments;


  // 내부 클래스 : 시험 정보
  @Getter
  @Builder
  public static class ExamInfo {
    private final Long examId;
    private final String examTitle;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String status;
  }

  // 내부 클래스 : 과제 정보
  @Getter
  @Builder
  public static class AssignmentInfo {
    private final Long assignmentId;
    private final String assignmentTitle;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final AssignmentSubmissionStatus status;
    private final Integer earnedScore;
  }

}
