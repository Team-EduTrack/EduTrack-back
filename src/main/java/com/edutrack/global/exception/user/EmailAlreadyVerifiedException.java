package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class EmailAlreadyVerifiedException extends UserSignupException {

  public EmailAlreadyVerifiedException() {
    super("이미 이메일 인증이 완료된 사용자입니다.", HttpStatus.BAD_REQUEST);
  }
}
