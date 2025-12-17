package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.AcademyVerifyRequest;
import com.edutrack.domain.user.dto.SignupInitResponse;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;

public interface UserService {

  SignupInitResponse signupRequest(SignupRequest request);

  void verifyAcademyCode(AcademyVerifyRequest request);

  SignupResponse completeSignup(String email);

}
