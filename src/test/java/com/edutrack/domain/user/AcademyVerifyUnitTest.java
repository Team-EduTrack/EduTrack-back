package com.edutrack.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.AcademyVerifyRequest;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.service.UserServiceImpl;
import com.edutrack.global.exception.user.TempUserNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AcademyVerifyUnitTest {

  @InjectMocks
  private UserServiceImpl userService;

  @Mock
  private TempUserRedisRepository tempUserRedisRepository;

  @Mock
  private AcademyRepository academyRepository;

  @Test
  @DisplayName("학원 코드 인증 성공 시 TempUser 에 academyCode 가 저장된다")
  void academy_verify_success() {

    // given
    String signupToken = "signup-token";
    String academyCode = "ACADEMY123";

    TempUser tempUser = TempUser.builder()
        .signupToken(signupToken)
        .verified(true)
        .build();

    User principalUser = mock(User.class);

    Academy academy = new Academy(
        "에듀트랙 학원",
        academyCode,
        principalUser);


    when(tempUserRedisRepository.findBySignupToken(signupToken))
        .thenReturn(tempUser);

    when(academyRepository.findByCode(academyCode))
        .thenReturn(Optional.of(academy));

    // when
    userService.verifyAcademyCode(
        new AcademyVerifyRequest(signupToken, academyCode)
    );

    // then
    assertThat(tempUser.getAcademyCode()).isEqualTo(academyCode);

    verify(tempUserRedisRepository)
        .save(eq(tempUser), anyLong());
  }

  @Test
  @DisplayName("TempUser 가 없으면 예외 발생")
  void academy_verify_fail_tempUser_not_found() {

    when(tempUserRedisRepository.findBySignupToken(any()))
        .thenReturn(null);

    org.junit.jupiter.api.Assertions.assertThrows(
        TempUserNotFoundException.class,
        () -> userService.verifyAcademyCode(
            new AcademyVerifyRequest("token", "ACADEMY")
        )
    );
  }

}
