package com.edutrack.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AcademyVerifyRequest {

  private String signupToken;
  private String academyCode;

}
