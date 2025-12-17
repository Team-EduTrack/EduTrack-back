package com.edutrack.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserEmailVerificationRedisRepository;
import com.edutrack.domain.user.service.EmailVerificationServiceImpl;
import com.edutrack.global.exception.user.VerificationCodeMismatchException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceUnitTest {

  @InjectMocks
  private EmailVerificationServiceImpl emailVerificationService;

  @Mock
  private TempUserRedisRepository tempUserRedisRepository;

  @Mock
  private UserEmailVerificationRedisRepository userEmailVerificationRedisRepository;

  @Test
  @DisplayName("이메일 인증 성공 시 TempUser 가 verified=true 로 변경된다")
  void verify_email_success() {

    // given
    String signupToken = UUID.randomUUID().toString();
    String email = "test@test.com";
    String code = "123456";

    TempUser tempUser = TempUser.builder()
        .signupToken(signupToken)
        .email(email)
        .verified(false)
        .build();

    when(tempUserRedisRepository.findBySignupToken(signupToken))
        .thenReturn(tempUser);
    when(userEmailVerificationRedisRepository.getCode(email))
        .thenReturn(code);

    // when
    emailVerificationService.verifyEmail(signupToken, code);

    // then
    assertThat(tempUser.isVerified()).isTrue();
    verify(tempUserRedisRepository).save(eq(tempUser), anyLong());
    verify(userEmailVerificationRedisRepository).deleteCode(email);
  }

  @Test
  @DisplayName("인증 코드가 다르면 예외 발생")
  void verify_email_fail_code_mismatch() {

    // given
    TempUser tempUser = TempUser.builder()
        .signupToken("token")
        .email("test@test.com")
        .verified(false)
        .build();

    when(tempUserRedisRepository.findBySignupToken(any()))
        .thenReturn(tempUser);
    when(userEmailVerificationRedisRepository.getCode(any()))
        .thenReturn("111111");

    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(
        VerificationCodeMismatchException.class,
        () -> emailVerificationService.verifyEmail("token", "999999")
    );
  }

}
