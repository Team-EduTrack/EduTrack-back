package com.edutrack.domain.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureDetailWithStatisticsResponse {

  private final LectureDetailForTeacherResponse lectureDetailForTeacherResponse;
  private final LectureStatisticsResponse lectureStatisticsResponse;
}
