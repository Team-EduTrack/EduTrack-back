package com.edutrack.domain.lecture.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentSearchRequest {
  private final String name;
  private final Long excludeLectureId;
}

