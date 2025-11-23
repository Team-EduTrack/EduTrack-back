package com.edutrack.domain.lecture.controller;

import com.edutrack.domain.lecture.dto.LectureForTeacherResponseDto;
import com.edutrack.domain.lecture.service.LectureService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureContorller {

  private final LectureService lectureService;

  @GetMapping()
  public ResponseEntity<List<LectureForTeacherResponseDto>> getLecturesByTeacherId(@RequestParam Long teacherId) {
    List<LectureForTeacherResponseDto> lectures = lectureService.getLecturesByTeacherId(teacherId);
    return ResponseEntity.ok(lectures);
  }

}
