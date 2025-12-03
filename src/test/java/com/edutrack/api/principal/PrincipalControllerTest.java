package com.edutrack.api.principal;

import com.edutrack.domain.principal.PrincipalService;
import com.edutrack.domain.principal.dto.PrincipalRegistrationRequest;
import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrincipalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrincipalService principalService;

    private static int nameCounter = 0;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleType.ADMIN).isEmpty()) {
            roleRepository.save(new Role(null, RoleType.ADMIN));
        }
        if (roleRepository.findByName(RoleType.PRINCIPAL).isEmpty()) {
            roleRepository.save(new Role(null, RoleType.PRINCIPAL));
        }
        if (roleRepository.findByName(RoleType.TEACHER).isEmpty()) {
            roleRepository.save(new Role(null, RoleType.TEACHER));
        }
        if (roleRepository.findByName(RoleType.STUDENT).isEmpty()) {
            roleRepository.save(new Role(null, RoleType.STUDENT));
        }
    }

    private String generateUniqueKoreanName() {
        String[] suffixes = {"가", "나", "다", "라", "마", "바", "사", "아", "자", "차", "카", "타", "파", "하"};
        int index = nameCounter++ % suffixes.length;
        int num = nameCounter / suffixes.length;
        return "테스트원장" + suffixes[index] + (num > 0 ? suffixes[num % suffixes.length] : "");
    }

    private PrincipalRegistrationRequest createValidRequest() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        PrincipalRegistrationRequest request = new PrincipalRegistrationRequest();
        request.setPrincipalName(generateUniqueKoreanName());
        request.setLoginId("user" + uniqueId);
        request.setPassword("P@ssword1234");
        request.setPasswordConfirm("P@ssword1234");
        request.setPhone("010" + String.format("%08d", (int)(Math.random() * 100000000)));
        request.setEmail(uniqueId + "@test.com");
        request.setAcademyName("테스트학원" + uniqueId);
        return request;
    }

    @Nested
    @DisplayName("권한 검증 테스트")
    class AuthorizationTest {

        @Test
        @DisplayName("인증되지 않은 사용자 접근 시 403 Forbidden 반환해야 한다")
        void signup_fail_unauthenticated() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PRINCIPAL")
        @DisplayName("PRINCIPAL 역할로 접근 시 403 Forbidden 반환해야 한다")
        void signup_fail_forbidden_principal() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("TEACHER 역할로 접근 시 403 Forbidden 반환해야 한다")
        void signup_fail_forbidden_teacher() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("STUDENT 역할로 접근 시 403 Forbidden 반환해야 한다")
        void signup_fail_forbidden_student() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("원장 등록 성공 테스트")
    @WithMockUser(roles = "ADMIN")
    class SignupSuccessTest {

        @Test
        @DisplayName("ADMIN 역할로 정상 요청 시 201 Created 응답과 학원 코드를 반환해야 한다")
        void registerAcademy_success() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.academyCode").exists())
                    .andExpect(jsonPath("$.academyName").value(request.getAcademyName()))
                    .andExpect(jsonPath("$.id").isNumber());
        }
    }

    @Nested
    @DisplayName("원장 등록 실패 테스트 - 비즈니스 로직")
    @WithMockUser(roles = "ADMIN")
    class SignupBusinessFailTest {

        @Test
        @DisplayName("비밀번호 불일치 시 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_password_mismatch() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setPasswordConfirm("Different123!");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-002"));
        }

        @Test
        @DisplayName("아이디 중복 시 409 Conflict 반환해야 한다")
        void registerAcademy_fail_duplicate_loginId() throws Exception {
            // given - 먼저 등록
            PrincipalRegistrationRequest firstRequest = createValidRequest();
            principalService.registerAcademy(firstRequest);

            // when - 같은 아이디로 다시 등록 시도
            PrincipalRegistrationRequest secondRequest = createValidRequest();
            secondRequest.setLoginId(firstRequest.getLoginId()); // 중복 아이디

            // then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("R-001"))
                    .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다."));
        }

        @Test
        @DisplayName("전화번호 중복 시 409 Conflict 반환해야 한다")
        void registerAcademy_fail_duplicate_phone() throws Exception {
            // given - 먼저 등록
            PrincipalRegistrationRequest firstRequest = createValidRequest();
            principalService.registerAcademy(firstRequest);

            // when - 같은 전화번호로 다시 등록 시도
            PrincipalRegistrationRequest secondRequest = createValidRequest();
            secondRequest.setPhone(firstRequest.getPhone()); // 중복 전화번호

            // then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("R-001"))
                    .andExpect(jsonPath("$.message").value("이미 등록된 전화번호입니다."));
        }

        @Test
        @DisplayName("이메일 중복 시 409 Conflict 반환해야 한다")
        void registerAcademy_fail_duplicate_email() throws Exception {
            // given - 먼저 등록
            PrincipalRegistrationRequest firstRequest = createValidRequest();
            principalService.registerAcademy(firstRequest);

            // when - 같은 이메일로 다시 등록 시도
            PrincipalRegistrationRequest secondRequest = createValidRequest();
            secondRequest.setEmail(firstRequest.getEmail()); // 중복 이메일

            // then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("R-001"))
                    .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
        }
    }

    @Nested
    @DisplayName("원장 등록 실패 테스트 - DTO 유효성 검증")
    @WithMockUser(roles = "ADMIN")
    class SignupValidationFailTest {

        @Test
        @DisplayName("모든 필드가 유효하지 않을 때 400 Bad Request (G-001)를 반환해야 한다")
        void registerAcademy_fail_validation_error_all_fields() throws Exception {
            // given
            String invalidJson = "{\"principalName\":\"\"," +
                    "\"loginId\":\"sh\"," +
                    "\"password\":\"123\"," +
                    "\"passwordConfirm\":\"123\"," +
                    "\"phone\":\"01012345678\"," +
                    "\"email\":\"not-an-email\"," +
                    "\"academyName\":\"\"}";

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("원장 이름이 비어있을 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_empty_principalName() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setPrincipalName("");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("원장 이름이 한글이 아닐 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_invalid_principalName_pattern() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setPrincipalName("JohnDoe");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("아이디가 5자 미만일 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_short_loginId() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setLoginId("ab12"); // 4자

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("아이디가 15자 초과일 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_long_loginId() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setLoginId("abcdefghij123456"); // 16자

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("아이디에 특수문자가 포함되어 있을 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_invalid_loginId_pattern() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setLoginId("user@name");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("비밀번호가 조건을 만족하지 않을 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_weak_password() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setPassword("simple123"); // 대문자, 특수문자 없음
            request.setPasswordConfirm("simple123");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("전화번호 형식이 올바르지 않을 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_invalid_phone() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setPhone("010-1234-5678"); // 하이픈 포함

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않을 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_invalid_email() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setEmail("invalid-email");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }

        @Test
        @DisplayName("학원 이름이 비어있을 때 400 Bad Request 반환해야 한다")
        void registerAcademy_fail_empty_academyName() throws Exception {
            // given
            PrincipalRegistrationRequest request = createValidRequest();
            request.setAcademyName("");

            // when & then
            mockMvc.perform(post("/api/academy/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("G-001"));
        }
    }
}
