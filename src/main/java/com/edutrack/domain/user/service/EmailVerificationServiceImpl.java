package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.UserEmailVerification;
import com.edutrack.domain.user.repository.TempUserRepository;
import com.edutrack.domain.user.repository.UserEmailVerificationRepository;
import com.edutrack.global.mail.MailSendService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

  private final UserEmailVerificationRepository userEmailVerificationRepository;
  private final MailSendService mailSendService;
  private final TempUserRepository tempUserRepository;

  private static final int CODE_LENGTH = 6;
  private static final long CODE_EXPIRATION_MINUTES = 5;

  @Override
  public void sendVerificationCode(SendEmailVerificationRequest request) {

    TempUser tempUser = tempUserRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입 신청이 없습니다."));

    String code = generateCode();

    UserEmailVerification verification = UserEmailVerification.builder()
        .email(tempUser.getEmail())
        .token(code)
        .verified(false)
        .build();

    userEmailVerificationRepository.save(verification);

    String subject = "[EduTrack] 이메일 인증 코드 안내";
    String text = "인증 코드:" + code + "\n\n5분 이내에 입력해 주세요.";

    mailSendService.sendMail(tempUser.getEmail(), subject, text);
  }

  @Override
  public void verifyEmail(VerifyEmailRequest request) {

    TempUser tempUser = tempUserRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입 신청이 없습니다."));

    UserEmailVerification verification = userEmailVerificationRepository
        .findTopByEmailOrderByCreatedAtDesc(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("인증 정보가 존재하지 않습니다."));

    if (verification.isVerified()) {
      throw new IllegalArgumentException("이미 사용된 인증 코드입니다.");
    }

    LocalDateTime now = LocalDateTime.now();
    if (verification.getCreatedAt().isBefore(now.minusMinutes(CODE_EXPIRATION_MINUTES))) {
      throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해 주세요.");
    }

    if (!verification.getToken().equals(request.getToken())) {
      throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
    }

    // 인증 요청 엔티티 상태 업데이트 (verified = true)
    verification.markVerified();
    // User 이메일 인증 완료 상태 변경
    tempUser.markVerified();

    // 변경된 TempUser & Verification 저장
    userEmailVerificationRepository.save(verification);
    tempUserRepository.save(tempUser);
  }

  private String generateCode() {
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(random.nextInt(10));
    }
    return sb.toString();
  }

}
