package com.edutrack.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/academy/signup").hasRole("ADMIN")
                        .requestMatchers("/api/users/signin").permitAll()   // 로그인
                        .requestMatchers("/h2-console/**").permitAll()      // H2 콘솔
                        .requestMatchers("/api/users/me").authenticated()   // 내 정보 조회
                        .requestMatchers(HttpMethod.GET,
                                "/api/academies/{academyId}/users/search")
                        .hasAnyRole("ADMIN", "PRINCIPAL")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/academies/{academyId}/users/{userId}/role/{roleName}")
                        .hasAnyRole("ADMIN", "PRINCIPAL")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }

}