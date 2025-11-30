package com.edutrack.domain.lecture.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentSearchResponse {
  private final Long studentId;
  private final String name;
  private final String phone;
}

