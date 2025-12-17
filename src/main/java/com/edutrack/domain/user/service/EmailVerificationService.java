package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;

public interface EmailVerificationService {

  void sendVerificationCode(SendEmailVerificationRequest request);

  void verifyEmail(String signupToken, String inputToken);

}
