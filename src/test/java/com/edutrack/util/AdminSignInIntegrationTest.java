package com.edutrack.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.entity.UserToRole;
import com.edutrack.domain.user.entity.UserToRoleId;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.domain.user.repository.UserToRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminSignInIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  UserToRoleRepository userToRoleRepository;

  @BeforeEach
  void setUp() {
    Role adminRole = roleRepository.findByName(RoleType.ADMIN)
        .orElseGet(() -> roleRepository.save(
            Role.builder()
                .name(RoleType.ADMIN)
                .build()
        ));

    User admin = User.builder()
        .loginId("admin")
        .password(passwordEncoder.encode("admin1234"))
        .name("시스템관리자")
        .phone("01012345678")
        .email("admin@edutrack.com")
        .emailVerified(true)
        .userStatus(UserStatus.ACTIVE)
        .build();

    User savedUser = userRepository.save(admin);

    UserToRoleId id = new UserToRoleId(
        savedUser.getId(),
        adminRole.getId()
    );

    UserToRole userToRole = UserToRole.builder()
        .id(id)
        .user(savedUser)
        .role(adminRole)
        .build();

    userToRoleRepository.save(userToRole);
  }

  @Test
  void adminSignIn() throws Exception {

    String body = """
            {
              "loginId": "admin",
              "password": "admin1234"
            }
            """;

    mockMvc.perform(post("/api/users/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(jsonPath("$.user.role").value("ADMIN"));
  }

}
