package com.edutrack.global.exception.academy;

import com.edutrack.global.exception.user.UserSignupException;
import org.springframework.http.HttpStatus;

public class AcademyNotVerifiedException extends UserSignupException {

  public AcademyNotVerifiedException() {
    super("학원 코드 인증이 필요합니다..", HttpStatus.BAD_REQUEST);
  }
}
