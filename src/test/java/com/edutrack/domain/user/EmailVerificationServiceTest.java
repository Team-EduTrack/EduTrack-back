package com.edutrack.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.service.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EmailVerificationServiceTest {

  @Autowired
  private EmailVerificationService emailVerificationService;

  @Autowired
  private TempUserRedisRepository tempUserRedisRepository;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Test
  void emailVerify(){

    // ✅ given: TempUser 저장
    TempUser tempUser = TempUser.builder()
        .loginId("user1")
        .password("pw")
        .name("이름")
        .phone("01011112222")
        .email("email@test.com")
        .verified(false)
        .build();

    tempUserRedisRepository.save(tempUser, 300);

    // ✅ when: 인증 코드 발송
    emailVerificationService.sendVerificationCode(
        new SendEmailVerificationRequest("email@test.com")
    );

    // ✅ Redis 에 저장된 인증 코드 조회
    String code = stringRedisTemplate.opsForValue()
        .get("emailVerify:email@test.com");

    // ✅ when: 이메일 인증 수행
    emailVerificationService.verifyEmail(
        new VerifyEmailRequest("email@test.com", code)
    );

    // ✅ then: TempUser가 verified = true 인지 확인
    TempUser verifiedUser =
        tempUserRedisRepository.findByEmail("email@test.com");

    assertThat(verifiedUser.isVerified()).isTrue();
  }

}
