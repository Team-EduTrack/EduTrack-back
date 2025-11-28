package com.edutrack.domain.assignment.dto;

import lombok.Getter;

@Getter
public class AssignmentSubmitRequest {

  private String fileKey; // S3에 업로드된 key

}
