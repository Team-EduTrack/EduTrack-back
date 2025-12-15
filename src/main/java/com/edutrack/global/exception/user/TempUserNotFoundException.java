package com.edutrack.global.exception.user;

import org.springframework.http.HttpStatus;

public class TempUserNotFoundException extends UserSignupException {

  public TempUserNotFoundException() {
    super("가입 신청 정보가 없습니다.", HttpStatus.NOT_FOUND);
  }
}
