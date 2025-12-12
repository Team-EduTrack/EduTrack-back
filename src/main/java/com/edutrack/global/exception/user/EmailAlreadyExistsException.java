package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends UserSignupException {

  public EmailAlreadyExistsException() {
    super("이미 등록된 이메일입니다.",HttpStatus.CONFLICT);
  }
}
