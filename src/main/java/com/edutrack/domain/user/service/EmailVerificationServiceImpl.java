package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserEmailVerification;
import com.edutrack.domain.user.repository.UserEmailVerificationRepository;
import com.edutrack.domain.user.repository.UserRepository;
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

  private final UserRepository userRepository;
  private final UserEmailVerificationRepository userEmailVerificationRepository;
  private final MailSendService mailSendService;

  private static final int CODE_LENGTH = 6;
  private static final long CODE_EXPIRATION_MINUTES = 5;

  @Override
  public void sendVerificationCode(SendEmailVerificationRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

    String code = generateCode();

    UserEmailVerification verification = UserEmailVerification.builder()
        .user(user)
        .token(code)
        .verified(false)
        .build();

    userEmailVerificationRepository.save(verification);

    String subject = "[EduTrack] 이메일 인증 코드 안내";
    String text = "인증 코드:" + code + "\n\n5분 이내에 입력해 주세요.";

    mailSendService.sendMail(user.getEmail(), subject, text);
  }

  @Override
  public void verifyEmail(VerifyEmailRequest request) {

    User user = userRepository.findByEmail((request.getEmail()))
        .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

    UserEmailVerification verification = userEmailVerificationRepository
        .findTopByUserOrderByCreatedAtDesc(user)
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

    verification.markVerified();
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
