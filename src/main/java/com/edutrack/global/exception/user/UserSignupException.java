package com.edutrack.global.exception.user;

import com.edutrack.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public abstract  class UserSignupException extends BusinessException {

  public UserSignupException(String message, HttpStatus status) {
    super(message,status);
  }
}
