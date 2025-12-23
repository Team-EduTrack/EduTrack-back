package com.edutrack.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentLectureAverageResponse {
  private final Double examAverageGrade;
  private final Double assignmentAverageScore;
  private final Long lectureId;
}
