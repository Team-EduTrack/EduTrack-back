package com.edutrack.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

  private String loginId;
  private String password;
  private String name;
  private String phone;
  private String email;
  private String academyCode;

}
