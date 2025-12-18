package com.edutrack.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;  // WebConfig의 Bean 주입

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))  // WebConfig의 Bean 사용
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 요청은 모든 경로에서 허용 (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/academy/signup").hasRole("ADMIN")
                        .requestMatchers("/api/users/signin").permitAll()   // 로그인
                        .requestMatchers("/h2-console/**").permitAll()      // H2 콘솔
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()  // Swagger UI
                        // /api/users/me는 authenticated이지만 OPTIONS는 이미 위에서 허용됨
                        .requestMatchers("/api/users/me").authenticated()   // 내 정보 조회
                        .requestMatchers(HttpMethod.GET,
                                "/api/academies/{academyId}/users/search")
                        .hasAnyRole("ADMIN", "PRINCIPAL", "TEACHER")
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