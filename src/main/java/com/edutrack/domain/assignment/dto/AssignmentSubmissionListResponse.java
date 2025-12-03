package com.edutrack.domain.assignment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignmentSubmissionListResponse {

  private Long submissionId;
  private Long studentId;
  private String studentName;
  private String fileUrl;
  private LocalDateTime submittedAt;

}
