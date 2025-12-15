package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredException extends UserSignupException {

  public VerificationCodeExpiredException() {
    super("인증 코드가 만료되었거나 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
  }
}
