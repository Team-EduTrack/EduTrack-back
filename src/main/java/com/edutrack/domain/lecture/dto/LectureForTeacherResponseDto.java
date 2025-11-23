package com.edutrack.domain.lecture.dto;

import com.edutrack.domain.lecture.entity.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureForTeacherResponseDto {
  private final Long lectureId;
  private final String title;
  private final int studentCount;

  public static LectureForTeacherResponseDto of(Lecture lecture, int studentCount) {
    return LectureForTeacherResponseDto.builder()
        .lectureId(lecture.getId())
        .title(lecture.getTitle())
        .studentCount(studentCount)
        .build();
  }

}
