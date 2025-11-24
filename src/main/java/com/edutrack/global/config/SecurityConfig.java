package com.edutrack.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        // 🔥 H2 콘솔을 위한 설정
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin()) // iframe 허용
        )

        // 🔥 H2 콘솔 접근 허용
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/api/auth/signup").permitAll()
            .anyRequest().permitAll()
        )

        // 개발 환경에서는 굳이 strict 보안 필요 없음
        .cors(cors -> cors.disable());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
