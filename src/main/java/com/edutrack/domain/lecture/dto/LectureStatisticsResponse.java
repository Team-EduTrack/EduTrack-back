package com.edutrack.domain.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureStatisticsResponse {

  private final Long lectureId;
  private final int studentCount;

  private final Double attendanceRate;
  private final Double assignmentSubmissionRate;
  private final Double examParticipationRate;

  private final Double averageScore;
  private final Double total10PercentScore;
}
