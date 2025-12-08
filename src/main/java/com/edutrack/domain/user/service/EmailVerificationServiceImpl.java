package com.edutrack.domain.user.service;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.UserEmailVerification;
import com.edutrack.domain.user.repository.SignupLockRepository;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserEmailVerificationRedisRepository;
import com.edutrack.global.mail.MailSendService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

  private final UserEmailVerificationRedisRepository userEmailVerificationRedisRepository;
  private final MailSendService mailSendService;
  private final TempUserRedisRepository tempUserRedisRepository;
  private final SignupLockRepository signupLockRepository;

  private static final long CODE_TTL = 5 * 60; // 5분

  @Override
  public void sendVerificationCode(SendEmailVerificationRequest request) {

    TempUser tempUser = tempUserRedisRepository.findByEmail(request.getEmail());
    if (tempUser == null) {
      throw new IllegalArgumentException("가입 신청이 없습니다.");
    }

    // 이미 인증된 사용자 재요청 차단
    if (tempUser.isVerified()) {
      throw new IllegalArgumentException("이미 이메일 인증이 완료되었습니다.");
    }

    // 재발급 제한 (이미 코드가 존재하면 차단)
    String alreadySentCode = userEmailVerificationRedisRepository.getCode(request.getEmail());
    if (alreadySentCode != null) {
      throw new IllegalArgumentException("이미 인증 코드가 발송 되었습니다. 잠시 후 다시 시도하세요.");
    }

    String code = generateCode();

    // Redis 에 인증코드 저장 (TTL = 5분)
    userEmailVerificationRedisRepository.saveCode(request.getEmail(), code, CODE_TTL);

    String subject = "[EduTrack] 이메일 인증 코드 안내";
    String text = "인증 코드:" + code + "\n5분 이내에 입력해 주세요.";

    mailSendService.sendMail(tempUser.getEmail(), subject, text);
  }

  @Override
  public void verifyEmail(VerifyEmailRequest request) {

    TempUser tempUser = tempUserRedisRepository.findByEmail(request.getEmail());
    if (tempUser == null) {
      throw new IllegalArgumentException("가입 신청이 없습니다.");
    }

    if (tempUser.isVerified()) {
      throw new IllegalArgumentException("이미 이메일 인증이 완료되었습니다.");
    }

    // Redis에 저장된 코드 가져오기
    String savedCode = userEmailVerificationRedisRepository.getCode(request.getEmail());
    if (savedCode == null) {
      throw new IllegalArgumentException("인증 코드가 만료되었거나 존재하지 않습니다.");
    }

    // 코드 비교
    if (!savedCode.equals(request.getToken())) {
      throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
    }

    // 이메일 인증 성공
    tempUser.markVerified();

    // TempUser 다시 저장 (TTL 15분)
    tempUserRedisRepository.save(tempUser, 15 * 60);

    // 인증 코드 삭제
    userEmailVerificationRedisRepository.deleteCode(request.getEmail());
    // signupLock 즉시 해제
    signupLockRepository.unLockAll(
        tempUser.getLoginId(),
        tempUser.getEmail(),
        tempUser.getPhone());

  }

  private String generateCode() {
    SecureRandom random = new SecureRandom();
    int code = random.nextInt(900_000) + 100_000; // 100000 ~ 999999
    return String.valueOf(code);
  }

}
