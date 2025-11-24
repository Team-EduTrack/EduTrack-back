package com.edutrack.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendEmailVerificationRequest {

  private String email;

}
