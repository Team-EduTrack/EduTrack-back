package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class VerificationCodeMismatchException extends UserSignupException {

  public VerificationCodeMismatchException() {
    super("인증 코드가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
  }
}
