package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class RoleNotPreparedException extends UserSignupException {

  public RoleNotPreparedException() {
    super("학생 역할(Role)이 사전에 세팅되어 있지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
