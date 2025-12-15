package com.edutrack.global.exception.user;


import org.springframework.http.HttpStatus;

public class LoginIdAlreadyExistsException extends UserSignupException {

  public LoginIdAlreadyExistsException() {
    super("이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT );
  }
}
