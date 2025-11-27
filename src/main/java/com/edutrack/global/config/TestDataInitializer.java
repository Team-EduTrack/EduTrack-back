package com.edutrack.global.config;

import com.edutrack.domain.user.entity.Role;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.entity.UserStatus;
import com.edutrack.domain.user.repository.RoleRepository;
import com.edutrack.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Postman 테스트용 학생 계정을 자동 생성하는 초기화 컴포넌트
 */

@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 이미 초기 데이터가 있으면 생성하지 않음
        if (userRepository.findByLoginId("student1").isPresent()) {
            return;
        }

        // 1. STUDENT 역할 불러오기
        Role studentRole = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT 역할이 DB에 없습니다."));

        // 2. 테스트 학생 생성
        User student = User.builder()
                .loginId("student1")
                .password(passwordEncoder.encode("1234"))
                .name("테스트학생")
                .phone("01011112222")
                .email("student1@test.com")
                .emailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        User savedStudent = userRepository.save(student);

        // UserToRole 매핑
        savedStudent.addRole(studentRole);
        userRepository.save(savedStudent);

        System.out.println(">>> [Test Init] STUDENT 계정 생성 완료: student1 / 1234");
    }
}
