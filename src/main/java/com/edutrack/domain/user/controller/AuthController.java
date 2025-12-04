package com.edutrack.domain.user.controller;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.service.EmailVerificationService;
import com.edutrack.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final EmailVerificationService emailVerificationService;

  // 회원가입
  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
    userService.signup(request);
    return ResponseEntity.ok("가입 신청이 완료되었습니다. 이메일 인증을 해주세요.");
  }

  // 이메일 인증 코드 발송
  @PostMapping("/send-email-verification")
  public ResponseEntity<String> sendEmailVerification(
      @RequestBody SendEmailVerificationRequest request) {
    emailVerificationService.sendVerificationCode(request);
    return ResponseEntity.ok("인증 코드가 발송되었습니다.");
  }

  // 이메일 인증 확인
  @PostMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
    emailVerificationService.verifyEmail(request);
    return ResponseEntity.ok("이메일 인증이 완료되었습니다. 진행중이던 회원가입을 완료하세요.");
  }

  // 최종 회원가입 완료
  @PostMapping("/signup/complete")
  public ResponseEntity<SignupResponse> completeSignup(@RequestBody VerifyEmailRequest request){
    SignupResponse response = userService.completeSignup(request.getEmail());
    return ResponseEntity.ok(response);

  }
}
