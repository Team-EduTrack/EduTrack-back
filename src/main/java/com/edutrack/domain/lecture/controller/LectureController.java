package com.edutrack.domain.lecture.controller;

import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.service.LectureService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

  private final LectureService lectureService;

  //강의 목록 조회 (선생용)
  @GetMapping
  public ResponseEntity<List<LectureForTeacherResponse>> getLecturesByTeacherId(@RequestParam Long teacherId) {
    List<LectureForTeacherResponse> lectures = lectureService.getLecturesByTeacherId(teacherId);
    return ResponseEntity.ok(lectures);
  }

  //강의 상세 조회 (선생용)
  @GetMapping("/{lectureId}")
  public ResponseEntity<LectureDetailForTeacherResponse> getLectureDetailForTeacherId(@RequestParam @PathVariable Long lectureId) {
    LectureDetailForTeacherResponse lecture = lectureService.getLectureDetailForTeacherId(lectureId);
    return ResponseEntity.ok(lecture);
  }
  }
