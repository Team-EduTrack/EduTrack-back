package com.edutrack.api.principal;

import com.edutrack.domain.principal.PrincipalService;
import com.edutrack.domain.principal.dto.PrincipalRegistrationRequest;
import com.edutrack.domain.user.Role;
import com.edutrack.domain.user.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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


    @BeforeEach
    void setUp() {
        if (roleRepository.findByName("PRINCIPAL").isEmpty()) {
            roleRepository.save(new Role("PRINCIPAL"));
        }
        if (roleRepository.findByName("TEACHER").isEmpty()) {
            roleRepository.save(new Role("TEACHER"));
        }
        if (roleRepository.findByName("STUDENT").isEmpty()) {
            roleRepository.save(new Role("STUDENT"));
        }
    }

    private PrincipalRegistrationRequest createValidRequest() {
        PrincipalRegistrationRequest request = new PrincipalRegistrationRequest();
        request.setPrincipalName("테스트원장");
        request.setLoginId("validtestuser");
        request.setPassword("P@ssword1234");
        request.setPasswordConfirm("P@ssword1234");
        request.setPhone("01011112222");
        request.setEmail("valid@test.com");
        request.setAcademyName("테스트학원");
        return request;
    }

    @Test
    @DisplayName("원장 등록 성공: 정상 요청 시 201 Created 응답과 학원 코드를 반환해야 한다.")
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
                .andExpect(jsonPath("$.academyName").value("테스트학원"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("원장 등록 실패: 비밀번호 불일치 시 400 Bad Request 반환해야 한다.")
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
    @DisplayName("원장 등록 실패: 아이디 중복 시 400 Bad Request 반환해야 한다.")
    void registerAcademy_fail_duplicate_loginId() throws Exception {
        // given (Service를 통해 1차 등록하여 중복 데이터 생성)
        PrincipalRegistrationRequest dupRequest = createValidRequest();
        dupRequest.setLoginId("dup_user_id");
        dupRequest.setPhone("01099990000");
        dupRequest.setEmail("dup@email.com");

        principalService.registerAcademy(dupRequest);

        // when
        PrincipalRegistrationRequest request = createValidRequest();
        request.setLoginId("dup_user_id");
        request.setPhone("01088880000");
        request.setEmail("new@email.com");

        // then
        mockMvc.perform(post("/api/academy/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("R-001"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다."));
    }

    @Test
    @DisplayName("원장 등록 실패: DTO 유효성 검증 실패 시 400 Bad Request (G-001)를 반환해야 한다.")
    void registerAcademy_fail_validation_error() throws Exception {
        // given

        String invalidJson = "{\"principalName\":\"\"," + // @NotBlank 위반
                "\"loginId\":\"sh\"," +        // @Size(min=5) 위반
                "\"password\":\"123\"," +       // @Pattern 위반
                "\"passwordConfirm\":\"123\"," +
                "\"phone\":\"01012345678\"," +
                "\"email\":\"not-an-email\"," + // @Email 위반
                "\"academyName\":\"\"}"; // @NotBlank 위반

        // when & then
        mockMvc.perform(post("/api/academy/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 Bad Request 확인
                .andExpect(jsonPath("$.errorCode").value("G-001"));
    }
}