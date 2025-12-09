package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.lecture.dto.LectureStatisticsResponse;
import com.edutrack.domain.statistics.service.LectureStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureStatisticsController {

  private final LectureStatisticsService lectureStatisticsService;

  /**
   * 강의 단위 전체 통계 조회
   * 수강생 수, 출석률, 과제 제출률, 시험 응시율, 평균 성적, 상위 10% 평균 성적
   */
  @GetMapping("/{lectureId}/statistics")
  @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
  public ResponseEntity<LectureStatisticsResponse> getLectureStatistics(
      @PathVariable Long lectureId,
      Authentication authentication) {

    Long teacherId = (Long) authentication.getPrincipal();
    LectureStatisticsResponse response = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

    return ResponseEntity.ok(response);
  }
}
