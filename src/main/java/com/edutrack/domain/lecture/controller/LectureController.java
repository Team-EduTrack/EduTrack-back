package com.edutrack.domain.lecture.controller;

import com.edutrack.domain.lecture.dto.LectureCreationRequest;
import com.edutrack.domain.lecture.dto.LectureCreationResponse;
import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.service.LectureCreationService;
import com.edutrack.domain.lecture.service.LectureService;
import com.edutrack.domain.user.entity.User;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

  private final LectureService lectureService;
  private final LectureCreationService lectureCreationService;

  // 강의 생성 API

  @PostMapping

  @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
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

  //강의 목록 조회 (선생용)
  @PreAuthorize(("hasRole('TEACHER') or hasRole('PRINCIPAL')"))
  @GetMapping
  public ResponseEntity<List<LectureForTeacherResponse>> getLecturesByTeacherId(Authentication authentication) {
    Long teacherId = (Long) authentication.getPrincipal();
    List<LectureForTeacherResponse> lectures = lectureService.getLecturesByTeacherId(teacherId);
    return ResponseEntity.ok(lectures);
  }

  //강의 상세 조회 (선생용)
  @PreAuthorize(("hasRole('TEACHER') or hasRole('PRINCIPAL')"))
  @GetMapping("/{lectureId}")
  public ResponseEntity<LectureDetailForTeacherResponse> getLectureDetailForTeacherId(
      @PathVariable Long lectureId,
      Authentication authentication) {
    Long teacherId = (Long) authentication.getPrincipal();
    LectureDetailForTeacherResponse lectureDetail = lectureService.getLectureDetailForTeacherId(lectureId, teacherId);
    return ResponseEntity.ok(lectureDetail);
    }
  }
