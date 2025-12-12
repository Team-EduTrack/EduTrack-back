package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class PhoneAlreadyExistsException extends UserSignupException {

  public PhoneAlreadyExistsException() {
    super("이미 등록된 전화번호입니다.", HttpStatus.CONFLICT);
  }
}
