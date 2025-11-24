package com.edutrack.api.lecture.controller;

import com.edutrack.api.lecture.dto.LectureCreationRequest;
import com.edutrack.api.lecture.dto.LectureCreationResponse;
import com.edutrack.api.lecture.service.LectureCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureCreationService lectureCreationService;

    @PostMapping
    public ResponseEntity<LectureCreationResponse> createLecture(
            @Valid @RequestBody LectureCreationRequest request,

            Authentication authentication) {


        Long principalUserId = (Long) authentication.getPrincipal();


        Long lectureId = lectureCreationService.createLecture(principalUserId, request);

        LectureCreationResponse response = LectureCreationResponse.builder()
                .lectureId(lectureId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}