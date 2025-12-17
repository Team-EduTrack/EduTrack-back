package com.edutrack.global.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println(">>> JwtFilter 요청: " + request.getMethod() + " " + request.getRequestURI());

        // OPTIONS 요청은 CORS preflight이므로 필터를 건너뜀
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println(">>> JwtFilter Authorization header = " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println(">>> JwtFilter token = " + token);

        Long userId;
        try {
            userId = jwtTokenProvider.getUserIdFromToken(token);
            System.out.println(">>> JwtFilter userId from token = " + userId);
        } catch (Exception e) {
            System.out.println(">>> JwtFilter getUserIdFromToken 실패: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        String roleName;
        try {
            roleName = jwtTokenProvider.getRoleFromToken(token);
        } catch (Exception e) {
            System.out.println(">>> JwtFilter getRoleFromToken 실패: " + e.getMessage());
            roleName = "STUDENT";
        }

        System.out.println(">>> JwtFilter user role(from token) = " + roleName);

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + roleName);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(authority)
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}