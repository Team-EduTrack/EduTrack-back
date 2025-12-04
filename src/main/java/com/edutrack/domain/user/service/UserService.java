package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;

public interface UserService {

  void signup(SignupRequest request);

  SignupResponse completeSignup(String email);

}
