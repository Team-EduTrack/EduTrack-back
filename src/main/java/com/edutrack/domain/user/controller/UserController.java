package com.edutrack.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrack.domain.user.dto.MyInfoResponse;
import com.edutrack.domain.user.dto.SignInRequest;
import com.edutrack.domain.user.dto.SignInResponse;
import com.edutrack.domain.user.service.UserAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserAuthService userAuthService;

    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest request) {
        SignInResponse response = userAuthService.signIn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> me(
            @AuthenticationPrincipal Long userId
    ) {
        MyInfoResponse response = userAuthService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }
}


