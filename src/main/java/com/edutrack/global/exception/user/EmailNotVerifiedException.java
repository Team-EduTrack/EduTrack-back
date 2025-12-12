package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends UserSignupException {

  public EmailNotVerifiedException() {
    super("이메일 인증이 완료되지 않았습니다.", HttpStatus.BAD_REQUEST);
  }
}
