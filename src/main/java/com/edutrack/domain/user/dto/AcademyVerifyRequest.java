package com.edutrack.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AcademyVerifyRequest {

  private String email;
  private String academyCode;

}
