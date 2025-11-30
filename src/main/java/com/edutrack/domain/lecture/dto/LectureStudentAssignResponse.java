package com.edutrack.domain.lecture.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureStudentAssignResponse {
  private Long lectureId;
  private int assignedCount;
}
