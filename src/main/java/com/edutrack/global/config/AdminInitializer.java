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
 * 애플리케이션 시작 시점에 ADMIN 계정을 자동으로 생성하고 권한을 부여하는 초기화 컴포넌트입니다.
 * 이 계정은 Postman 검증 및 시스템 최고 권한 테스트에 사용됩니다.
 */
@Component // 스프링 빈으로 등록
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    private static final String ADMIN_LOGIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin@1234";

    /**
     * 서버 시작 후 단 1회 실행되는 메서드입니다.
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (userRepository.findByLoginId(ADMIN_LOGIN_ID).isEmpty()) {


            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ADMIN 역할이 DB에 존재하지 않습니다. data.sql을 확인하세요."));


            String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);

            // 3. ADMIN 사용자 생성
            User admin = User.builder()
                    .loginId(ADMIN_LOGIN_ID)
                    .password(encodedPassword)
                    .name("시스템관리자")
                    .phone("01012345678")
                    .email("admin@edutrack.com")
                    .emailVerified(true)
                    .userStatus(UserStatus.ACTIVE)
//                    .roles(Set.of(adminRole)) -> 지금은 roles 필드가 없고 UserToRole 로 관리됨
                    .build();

            // User 저장 -> DB 에서 ID 생성됨
            User savedAdmin = userRepository.save(admin);

            // 추가 -> UserToRole 엔티티 생성을 위한 addRole() 도메인 메서드 사용
            // 기존 -> ManyToMany 방식이 아니라 지금 구조에서는 필수
            savedAdmin.addRole(adminRole);

            // 변경된 User (user_to_role 추가)를 다시 저장하여 매핑 완료
            userRepository.save(savedAdmin);
            System.out.println(">>> [System Init] ADMIN 계정 자동 생성 완료: ID=" + ADMIN_LOGIN_ID);
        }
    }
}