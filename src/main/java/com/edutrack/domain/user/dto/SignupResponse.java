package com.edutrack.domain.user.dto;

import com.edutrack.domain.user.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

  private Long id;
  private String loginId;
  private String name;
  private RoleType role;

}
