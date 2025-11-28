package com.edutrack.domain.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignmentSubmitResponse {

  private Long submissionId;
  private String message;
  private String fileUrl;

}
