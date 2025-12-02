package com.edutrack.domain.lecture.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LectureStudentAssignRequest {
  @NotEmpty
  private List<Long> studentIds;
}
