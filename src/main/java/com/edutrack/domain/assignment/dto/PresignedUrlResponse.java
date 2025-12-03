package com.edutrack.domain.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {

  private String presignedUrl; // 파일 업로드 Url;
  private String fileKey;       // DB에 저장할 key

}
