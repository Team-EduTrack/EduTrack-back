package com.edutrack.domain.lecture.dto;


import com.edutrack.domain.lecture.entity.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureForStudentResponseDto {
  private final Long lectureId;
  private final String title;
  private final String teacherName;

  public static LectureForStudentResponseDto of(Lecture lecture) {
    return LectureForStudentResponseDto.builder()
        .lectureId(lectureId)
        .title(title)
        .teacherName(teacherName)
        .build();
  }
}
