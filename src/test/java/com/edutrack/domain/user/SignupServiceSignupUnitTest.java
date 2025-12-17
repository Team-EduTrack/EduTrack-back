package com.edutrack.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edutrack.domain.user.dto.SignupInitResponse;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.repository.SignupLockRepository;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.service.UserServiceImpl;
import com.edutrack.global.exception.user.LoginIdAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SignupServiceSignupUnitTest {

  @InjectMocks
  private UserServiceImpl userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TempUserRedisRepository tempUserRedisRepository;

  @Mock
  private SignupLockRepository signupLockRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("회원가입 요청 시 signupToken 이 생성되고 TempUser 가 Redis에 저장된다")
  void signup_request_success() {

    // given
    SignupRequest request = new SignupRequest(
        "loginId",
        "password",
        "홍길동",
        "01012345678",
        "test@test.com"
    );

    when(userRepository.existsByLoginId(any())).thenReturn(false);
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.existsByPhone(any())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("ENCODED_PASSWORD");

    // when
    SignupInitResponse response = userService.signupRequest(request);

    // then
    assertThat(response.getSignupToken()).isNotNull();

    ArgumentCaptor<TempUser> captor = ArgumentCaptor.forClass(TempUser.class);
    verify(tempUserRedisRepository).save(captor.capture(), anyLong());

    TempUser savedTempUser = captor.getValue();
    assertThat(savedTempUser.getSignupToken()).isEqualTo(response.getSignupToken());
    assertThat(savedTempUser.getLoginId()).isEqualTo("loginId");
    assertThat(savedTempUser.isVerified()).isFalse();
  }

  @Test
  @DisplayName("이미 존재하는 loginId 이면 예외 발생")
  void signup_request_fail_duplicate_loginId() {

    // given
    SignupRequest request = new SignupRequest(
        "loginId",
        "password",
        "홍길동",
        "01012345678",
        "test@test.com"
    );

    when(userRepository.existsByLoginId(any())).thenReturn(true);

    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(
        LoginIdAlreadyExistsException.class,
        () -> userService.signupRequest(request)
    );
  }

}
