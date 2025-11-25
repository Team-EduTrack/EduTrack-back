package com.edutrack.domain.lecture.controller;

import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.service.LectureService;
import com.edutrack.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

  private final LectureService lectureService;

  //강의 목록 조회 (선생용)
  @PreAuthorize(("hasRole('TEACHER') or hasRole('PRINCIPAL')"))
  @GetMapping
  public ResponseEntity<List<LectureForTeacherResponse>> getLecturesByTeacherId(@AuthenticationPrincipal User user) {
    Long teacherId = user.getId();
    List<LectureForTeacherResponse> lectures = lectureService.getLecturesByTeacherId(teacherId);
    return ResponseEntity.ok(lectures);
  }

  //강의 상세 조회 (선생용)
  @PreAuthorize(("hasRole('TEACHER') or hasRole('PRINCIPAL')"))
  @GetMapping("/{lectureId}")
  public ResponseEntity<LectureDetailForTeacherResponse> getLectureDetailForTeacherId(
      @PathVariable Long lectureId,
      @AuthenticationPrincipal User user) {
    LectureDetailForTeacherResponse lectureDetail = lectureService.getLectureDetailForTeacherId(lectureId, user);
    return ResponseEntity.ok(lectureDetail);
  }
  }
