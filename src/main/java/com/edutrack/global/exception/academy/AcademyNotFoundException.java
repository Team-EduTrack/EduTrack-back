package com.edutrack.global.exception.academy;

import com.edutrack.global.exception.user.UserSignupException;
import org.springframework.http.HttpStatus;

public class AcademyNotFoundException extends UserSignupException {

  public AcademyNotFoundException() {
    super("학원 코드가 올바르지 않습니다.", HttpStatus.NOT_FOUND);
  }
}
