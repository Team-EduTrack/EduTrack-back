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
    String email = request.getEmail();

    // 1. ✨ 수정: 이메일 중복 확인 (회원가입 전에 이미 DB에 User가 있으면 중복 에러 발생)
    if (userRepository.findByEmail(email).isPresent()) {
      // 409 Conflict에 해당하는 예외 메시지
      throw new IllegalArgumentException("이미 가입된 이메일입니다.");
    }

    // 2. 인증 코드 생성 및 저장 (User 없이 email만 사용)
    String code = generateCode();

    UserEmailVerification verification = UserEmailVerification.builder()
            // .user(null) // User 필드는 null (엔티티 수정 전제)
            .email(email) // ✨ email 필드에 저장
            .token(code)
            .verified(false)
            .build();

    userEmailVerificationRepository.save(verification);

    String subject = "[EduTrack] 이메일 인증 코드 안내";
    String text = "인증 코드:" + code + "\n\n5분 이내에 입력해 주세요.";

    mailSendService.sendMail(email, subject, text);
  }

  @Override
  public void verifyEmail(VerifyEmailRequest request) {
    String email = request.getEmail();

    // 1. ✨ 수정: User 엔티티 없이 email만 사용하여 인증 정보를 찾습니다.
    UserEmailVerification verification = userEmailVerificationRepository
            // 레포지토리 메소드 이름이 findTopByEmailOrderByCreatedAtDesc 와 같이 변경되어야 함
            .findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new IllegalArgumentException("인증 정보가 존재하지 않습니다. 이메일을 다시 요청해 주세요."));

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