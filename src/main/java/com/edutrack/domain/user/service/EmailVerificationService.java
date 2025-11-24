package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.VerifyEmailRequest;

public interface EmailVerificationService {

  void sendVerificationCode(SendEmailVerificationRequest request);

  void verifyEmail(VerifyEmailRequest request);

}
