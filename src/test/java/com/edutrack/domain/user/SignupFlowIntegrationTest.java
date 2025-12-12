package com.edutrack.domain.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.user.dto.AcademyVerifyRequest;
import com.edutrack.domain.user.dto.CompleteSignupRequest;
import com.edutrack.domain.user.dto.SendEmailVerificationRequest;
import com.edutrack.domain.user.dto.SignupRequest;
import com.edutrack.domain.user.dto.VerifyEmailRequest;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class SignupFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AcademyRepository academyRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Test
  void signup() throws Exception {

    // 1. Principal User 저장
    User principal = User.builder()
        .loginId("principal1")
        .password("1234")
        .name("원장")
        .phone("01000000000")
        .email("principal@test.com")
        .build();
    userRepository.save(principal);

// 2. Academy 저장 (principal 필수)
    Academy academy = new Academy("테스트 학원", "EDU-1234", principal);
    academyRepository.save(academy);

//    3. STUDENT 역할
    Role studentRole = Role.builder()
        .name(RoleType.STUDENT)
        .build();
    roleRepository.save(studentRole);

    // 1단계 : 회원가입 요청
    SignupRequest signupRequest = new SignupRequest();
    signupRequest.setLoginId("testuser1");
    signupRequest.setPassword("1234");
    signupRequest.setName("테스틑");
    signupRequest.setPhone("01099998888");
    signupRequest.setEmail("test1@test.com");

    mockMvc.perform(post("/api/auth/signup/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk());

    // 2단계 : 이메일 인증 코드 발송
    SendEmailVerificationRequest request = new SendEmailVerificationRequest("test1@test.com");

    mockMvc.perform(post("/api/auth/send-email-verification")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // 3단계 : Redis 에서 인증 코드 직접 조회
    String code = stringRedisTemplate.opsForValue().get("emailVerify:test1@test.com");

    VerifyEmailRequest verifyEmailRequest = new VerifyEmailRequest("test1@test.com", code);

    mockMvc.perform(post("/api/auth/verify-email")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(verifyEmailRequest)))
        .andExpect(status().isOk());

    // 4단계 : 학원 코드 인증
    AcademyVerifyRequest academyVerifyRequest = new AcademyVerifyRequest("test1@test.com",
        "EDU-1234");

    mockMvc.perform(post("/api/auth/academy-verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(academyVerifyRequest)))
        .andExpect(status().isOk());

    // 5단계 : 최종 회원가입
    CompleteSignupRequest completeSignupRequest = new CompleteSignupRequest("test1@test.com");

    mockMvc.perform(post("/api/auth/signup/complete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(completeSignupRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.loginId").value("testuser1"))
        .andExpect(jsonPath("$.role").value("STUDENT"));
  }
}
