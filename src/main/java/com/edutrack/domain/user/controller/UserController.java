package com.edutrack.domain.user.controller;

import com.edutrack.domain.user.dto.MyInfoResponse;
import com.edutrack.domain.user.dto.SignInRequest;
import com.edutrack.domain.user.dto.SignInResponse;
import com.edutrack.domain.user.service.UserAuthService;
import com.edutrack.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserAuthService userAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest request) {
        SignInResponse response = userAuthService.signIn(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> me(
            @RequestHeader("Authorization") String authHeader
    ) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        MyInfoResponse response = userAuthService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }
}


