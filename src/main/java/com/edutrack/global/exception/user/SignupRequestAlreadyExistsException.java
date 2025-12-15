package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class SignupRequestAlreadyExistsException extends UserSignupException {

  public SignupRequestAlreadyExistsException(String field) {
    super("이미 가입 신청된 " + field + "입니다", HttpStatus.CONFLICT);
  }
}
