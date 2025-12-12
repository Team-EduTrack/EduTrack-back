package com.edutrack.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.AcademyVerifyRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserServiceTest {

  @Autowired
  private UserService userService;

  @Autowired
  private TempUserRedisRepository tempUserRedisRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AcademyRepository academyRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Test
  void academyCodeVerifyAndSignupComplete(){

    // ✅ given 1: STUDENT 역할 세팅
    Role studentRole = Role.builder()
        .name(RoleType.STUDENT)
        .build();
    roleRepository.save(studentRole);

    // ✅ given 2: 원장 유저 생성
    User principal = User.builder()
        .loginId("principal1")
        .password("1234")
        .name("원장")
        .phone("01000000000")
        .email("principal@test.com")
        .build();
    userRepository.save(principal);

    // ✅ given 3: 학원 생성
    Academy academy = new Academy(
        "테스트 학원",
        "EDU-1234",
        principal
    );
    academyRepository.save(academy);

    // ✅ given 4: 이메일 인증이 완료된 TempUser 생성
    TempUser tempUser = TempUser.builder()
        .loginId("user2")
        .password("pw")
        .name("이름")
        .phone("01022223333")
        .email("user2@test.com")
        .verified(true)
        .build();

    tempUserRedisRepository.save(tempUser, 300);

    // ✅ when 1: 학원 코드 인증
    userService.verifyAcademyCode(
        new AcademyVerifyRequest("user2@test.com", "EDU-1234")
    );

    // ✅ when 2: 최종 회원가입
    SignupResponse response =
        userService.completeSignup("user2@test.com");

    // ✅ then: STUDENT 역할 + User 생성 확인
    assertThat(response.getRole()).isEqualTo(RoleType.STUDENT);
    assertThat(userRepository.existsByLoginId("user2")).isTrue();
  }

}
