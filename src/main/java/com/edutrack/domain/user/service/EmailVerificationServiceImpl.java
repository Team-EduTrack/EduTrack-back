package com.edutrack.domain.user.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserEmailVerificationRedisRepository;
import com.edutrack.global.exception.user.EmailAlreadyVerifiedException;
import com.edutrack.global.exception.user.TempUserNotFoundException;
import com.edutrack.global.exception.user.VerificationCodeAlreadySentException;
import com.edutrack.global.exception.user.VerificationCodeExpiredException;
import com.edutrack.global.exception.user.VerificationCodeMismatchException;
import com.edutrack.global.mail.MailSendService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

  private final UserEmailVerificationRedisRepository userEmailVerificationRedisRepository;
  private final MailSendService mailSendService;
  private final TempUserRedisRepository tempUserRedisRepository;

  private static final long CODE_TTL = 5 * 60; // 5분

  @Override
  public void sendVerificationCode(SendEmailVerificationRequest request) {
    if (request.getSignupToken() == null || request.getSignupToken().trim().isEmpty()) {
      throw new TempUserNotFoundException();
    }

    TempUser tempUser = tempUserRedisRepository.findBySignupToken(request.getSignupToken());
    if (tempUser == null) {
      throw new TempUserNotFoundException();
    }

    // 이미 인증된 사용자 재요청 차단
    if (tempUser.isVerified()) {
      throw new EmailAlreadyVerifiedException();
    }

    // 재발급 제한 (이미 코드가 존재하면 차단)
    String alreadySentCode = userEmailVerificationRedisRepository.getCode(tempUser.getEmail());
    if (alreadySentCode != null) {
      throw new VerificationCodeAlreadySentException();
    }

    String code = generateCode();

    // Redis 에 인증코드 저장 (TTL = 5분)
    userEmailVerificationRedisRepository.saveCode(tempUser.getEmail(), code, CODE_TTL);

    String subject = "[EduTrack] 이메일 인증 코드 안내";
    String text = "인증 코드:" + code + "\n5분 이내에 입력해 주세요.";

    try {
      mailSendService.sendMail(tempUser.getEmail(), subject, text);
    } catch (Exception e) {
      System.err.println(">>> 메일 발송 실패: " + e.getMessage());
      e.printStackTrace();
      // 메일 발송 실패해도 Redis에 코드는 저장되었으므로 재시도 가능
      throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
    }
  }

  @Override
  public void verifyEmail(String signupToken, String inputCode) {

    TempUser tempUser = tempUserRedisRepository.findBySignupToken(signupToken);
    if (tempUser == null) {
      throw new TempUserNotFoundException();
    }

    if (tempUser.isVerified()) {
      throw new EmailAlreadyVerifiedException();
    }

    // Redis에 저장된 코드 가져오기
    String savedCode = userEmailVerificationRedisRepository.getCode(tempUser.getEmail());
    if (savedCode == null) {
      throw new VerificationCodeExpiredException();
    }

    // 코드 비교
    if (!savedCode.equals(inputCode)) {
      throw new VerificationCodeMismatchException();
    }

    // 이메일 인증 성공
    tempUser.markVerified();

    // TempUser 다시 저장 (TTL 15분)
    tempUserRedisRepository.save(tempUser, 15 * 60);

    // 인증 코드 삭제
    userEmailVerificationRedisRepository.deleteCode(tempUser.getEmail());
  }

  private String generateCode() {
    SecureRandom random = new SecureRandom();
    int code = random.nextInt(900_000) + 100_000; // 100000 ~ 999999
    return String.valueOf(code);
  }

}
