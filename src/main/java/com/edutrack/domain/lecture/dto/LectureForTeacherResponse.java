package com.edutrack.domain.lecture.dto;


import com.edutrack.domain.lecture.entity.Lecture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LectureForTeacherResponse {

  private final Long lectureId;
  private final String title;
  private final int studentCount;
  private final String teacherName;
  private final Double averageGrade;

  public static LectureForTeacherResponse of(Lecture lecture, int studentCount, Double averageScore) {
    return LectureForTeacherResponse.builder()
        .lectureId(lecture.getId())
        .title(lecture.getTitle())
        .studentCount(studentCount)
        .teacherName(null)  // 선생님은 자기 강의만 보니까 불필요
        .averageGrade(averageScore)
        .build();
  }

  // 원장용 오버로딩 (teacherName 포함)
  public static LectureForTeacherResponse of(Lecture lecture, int studentCount, String teacherName, Double averageScore) {
    return LectureForTeacherResponse.builder()
        .lectureId(lecture.getId())
        .title(lecture.getTitle())
        .studentCount(studentCount)
        .teacherName(teacherName)
        .averageGrade(averageScore)
        .build();
  }

}
