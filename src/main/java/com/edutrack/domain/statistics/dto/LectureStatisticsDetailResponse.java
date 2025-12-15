package com.edutrack.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureStatisticsDetailResponse {

  private final Long lectureId;
  private final int studentCount;
  private final Double attendanceRate;
  private final Double assignmentSubmissionRate;
  private final Double examParticipationRate;

  private final Double averageScore;
  private final Double total10PercentScore;

}
