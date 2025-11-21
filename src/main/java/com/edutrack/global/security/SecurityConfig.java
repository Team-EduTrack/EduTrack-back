package com.edutrack.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())      //기본 로그인 폼 제거
                .httpBasic(basic -> basic.disable())    //Basic Auth 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/signin").permitAll()   // 로그인 API 허용
                        .requestMatchers("/h2-console/**").permitAll()       // H2 콘솔 허용
                        .anyRequest().permitAll()
                );

        // H2 console 사용 시 frameOptions 비활성화
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}