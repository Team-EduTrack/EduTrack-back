package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class VerificationCodeAlreadySentException extends UserSignupException {

  public VerificationCodeAlreadySentException() {
    super("이미 인증 코드가 발송되었습니다. 잠시 후 다시 시도하세요.", HttpStatus.TOO_MANY_REQUESTS);
  }
}
