package com.edutrack.domain.user.controller;

import com.edutrack.domain.user.dto.SignInRequest;
import com.edutrack.domain.user.dto.SignInResponse;
import com.edutrack.domain.user.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}


