package com.edutrack.global.config;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

//테스트용 코드로 나중에 삭제
@Configuration
public class InitDataConfig {

    @Bean
    public CommandLineRunner initUser(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {

            String loginId = "testuser"; 
            String rawPassword = "Password123!"; 
            Long academyId = 1L;

            // 이미 있는지 체크
            if (userRepository.findByLoginId(loginId).isEmpty()) {

                User user = new User(
                        loginId,
                        encoder.encode(rawPassword),
                        "홍길동",
                        "01012345678",
                        "test@test.com",
                        academyId
                );

                userRepository.save(user);

                System.out.println("테스트 유저 생성 완료");
                System.out.println("로그인 ID: " + loginId);
                System.out.println("비밀번호: " + rawPassword);
            } else {
                System.out.println("테스트 유저가 이미 있습니다.");
            }
        };
    }
}