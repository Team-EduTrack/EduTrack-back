package com.edutrack.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrack.domain.user.dto.AcademyVerifyRequest;
import com.edutrack.domain.user.dto.CompleteSignupRequest;
import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.SignupInitResponse;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.service.EmailVerificationService;
import com.edutrack.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final EmailVerificationService emailVerificationService;

  // 회원가입
  @PostMapping("/signup/request")
  public ResponseEntity<SignupInitResponse> signup(@RequestBody SignupRequest request) {
    SignupInitResponse response = userService.signupRequest(request);
    return ResponseEntity.ok(response);
  }

  // 이메일 인증 코드 발송
  @PostMapping("/send-email-verification")
  public ResponseEntity<String> sendEmailVerification(
      @RequestBody SendEmailVerificationRequest request) {
    System.out.println(">>> AuthController.sendEmailVerification 호출됨!");
    System.out.println(">>> 요청 본문 signupToken: " + request.getSignupToken());
    emailVerificationService.sendVerificationCode(request);
    return ResponseEntity.ok("인증 코드가 발송되었습니다.");
  }

  // 이메일 인증 확인
  @PostMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
    emailVerificationService.verifyEmail(request.getSignupToken(),request.getInputCode());
    return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
  }

  // 학원 코드 인증
  @PostMapping("/academy-verify")
  public ResponseEntity<String> verifyAcademy(@RequestBody AcademyVerifyRequest request){
    userService.verifyAcademyCode(request);
    return ResponseEntity.ok("학원 코드 인증이 완료되었습니다.");
  }

  // 최종 회원가입 완료
  @PostMapping("/signup/complete")
  public ResponseEntity<SignupResponse> completeSignup(@RequestBody CompleteSignupRequest request){
    SignupResponse response = userService.completeSignup(request.getSignupToken());
    return ResponseEntity.ok(response);

  }
}
