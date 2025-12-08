package com.edutrack.domain.user;


import static org.assertj.core.api.Assertions.assertThat;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.SignupResponse;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.TempUser;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.SignupLockRepository;
import com.edutrack.domain.user.repository.TempUserRedisRepository;
import com.edutrack.domain.user.repository.UserEmailVerificationRedisRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.service.EmailVerificationServiceImpl;
import com.edutrack.domain.user.service.UserServiceImpl;
import com.edutrack.global.config.AdminInitializer;
import com.edutrack.global.mail.MailSendService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignupFlowIntegrationTest {

  @MockitoBean
  private AdminInitializer adminInitializer;

  @Autowired
  private UserServiceImpl userService;

  @Autowired
  private EmailVerificationServiceImpl emailService;

  @Autowired
  private AcademyRepository academyRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private TempUserRedisRepository tempUserRedisRepository;

  @Autowired
  private SignupLockRepository signupLockRepository;

  @Autowired
  private UserEmailVerificationRedisRepository userEmailVerificationRedisRepository;

  @Autowired
  private UserRepository userRepository;

  @MockitoBean
  private MailSendService mailSendService; // 실제 메일 발송 막기

  private final String email = "test107@naver.com";

  @BeforeEach
  void setupRoles() {
    if (roleRepository.count() == 0) {
      roleRepository.save(Role.builder().name(RoleType.ADMIN).build());
      roleRepository.save(Role.builder().name(RoleType.PRINCIPAL).build());
      roleRepository.save(Role.builder().name(RoleType.TEACHER).build());
      roleRepository.save(Role.builder().name(RoleType.STUDENT).build());
    }
  }


  @BeforeEach
  void setupAcademy() {
    User principal = userRepository.save(
        User.builder()
            .loginId("owner2")
            .password("ownerPw")
            .name("원장1")
            .phone("01099990002")
            .email("owner2@test.com")
            .build()
    );

    academyRepository.save(new Academy("테스트학원", "EDU-1000", principal));
  }

  @Test
  @Order(1)
  void signup() {

    // given
    SignupRequest req = new SignupRequest();
    req.setLoginId("testuser106");
    req.setPassword("1234@Aa!");
    req.setName("테스트유저106");
    req.setPhone("01011112236");
    req.setEmail(email);
    req.setAcademyCode("EDU-1000");

    // when
    userService.signup(req);

    // then: TempUser 저장 확인
    TempUser tempUser = tempUserRedisRepository.findByEmail(email);
    assertThat(tempUser).isNotNull();
    assertThat(tempUser.getLoginId()).isEqualTo("testuser106");
    assertThat(tempUser.isVerified()).isFalse();

    // then: Lock 저장 확인
    assertThat(signupLockRepository.existsByLoginId("testuser106")).isTrue();
    assertThat(signupLockRepository.existsByEmail(email)).isTrue();
    assertThat(signupLockRepository.existsByPhone("01011112236")).isTrue();
  }

  @Test
  @Order(2)
  void sendEmailVerification() {

    // when — 인증 코드 발송
    emailService.sendVerificationCode(new SendEmailVerificationRequest(email));

    // then — Redis에 인증 코드 저장되었는지 확인
    String savedCode = userEmailVerificationRedisRepository.getCode(email);
    assertThat(savedCode).isNotNull();
    assertThat(savedCode.length()).isEqualTo(6);  // 6자리 코드

    // then — 메일 발송이 호출되었는지 검증 (Mock)
    Mockito.verify(mailSendService)
        .sendMail(
            Mockito.eq(email),
            Mockito.anyString(),
            Mockito.contains("인증 코드")
        );
  }

  @Test
  @Order(3)
  void emailVerify() {

    // given — 회원가입 + 코드 발송
    emailService.sendVerificationCode(new SendEmailVerificationRequest(email));

    String code = userEmailVerificationRedisRepository.getCode(email);
    assertThat(code).isNotNull();

    VerifyEmailRequest verifyReq = new VerifyEmailRequest(email,code);

    // when — 이메일 인증 수행
    emailService.verifyEmail(verifyReq);

    // then — TempUser 검증
    TempUser tempUser = tempUserRedisRepository.findByEmail(email);
    assertThat(tempUser.isVerified()).isTrue();

    // then — 인증 코드 삭제되었는지 확인
    String deletedCode = userEmailVerificationRedisRepository.getCode(email);
    assertThat(deletedCode).isNull();
  }

  @Transactional
  @Test
  @Order(4)
  void signupComplete() {

    System.out.println(roleRepository.findAll());
    // given — 전체 과정 수행
    emailService.sendVerificationCode(new SendEmailVerificationRequest(email));

    String code = userEmailVerificationRedisRepository.getCode(email);

    emailService.verifyEmail(new VerifyEmailRequest(email, code));

    // when — 최종 회원가입 완료
    SignupResponse response = userService.completeSignup(email);

    // then — DB 저장 검증
    User user = userRepository.findByEmail(email).orElse(null);
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.isEmailVerified()).isTrue();
    assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);

    // then — TempUser 삭제 확인
    TempUser tempUser = tempUserRedisRepository.findByEmail(email);
    assertThat(tempUser).isNull();

    // then — 기본 role 부여 확인
    assertThat(user.getUserToRoles()).isNotEmpty();

  }




}
