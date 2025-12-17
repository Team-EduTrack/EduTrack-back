package com.edutrack.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.SignupLockRepository;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.service.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CompleteSignupUnitTest {

  @InjectMocks
  private UserServiceImpl userService;

  @Mock
  private TempUserRedisRepository tempUserRedisRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AcademyRepository academyRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private SignupLockRepository signupLockRepository;

  @Test
  @DisplayName("회원가입 완료 성공")
  void complete_signup_success() {

    // given
    String signupToken = "signup-token";
    String academyCode = "ACADEMY123";

    TempUser tempUser = TempUser.builder()
        .signupToken(signupToken)
        .loginId("loginId")
        .password("encodedPw")
        .name("홍길동")
        .phone("01012345678")
        .email("test@test.com")
        .verified(true)
        .academyCode(academyCode)
        .build();

    // 🔑 Academy 생성자에 맞춤
    User principalUser = mock(User.class);

    Academy academy = new Academy(
        "에듀트랙 학원",
        academyCode,
        principalUser
    );

    Role studentRole = new Role(null, RoleType.STUDENT);

    when(tempUserRedisRepository.findBySignupToken(signupToken))
        .thenReturn(tempUser);

    when(academyRepository.findByCode(academyCode))
        .thenReturn(Optional.of(academy));

    when(roleRepository.findByName(RoleType.STUDENT))
        .thenReturn(Optional.of(studentRole));

    when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    SignupResponse response = userService.completeSignup(signupToken);

    // then
    assertThat(response.getLoginId()).isEqualTo("loginId");
    assertThat(response.getRole()).isEqualTo(RoleType.STUDENT);

    verify(userRepository).save(any(User.class));
    verify(tempUserRedisRepository).deleteBySignupToken(signupToken);
  }

}
