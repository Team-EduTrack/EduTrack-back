package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.QuestionCorrectRateResponse;
import com.edutrack.domain.statistics.dto.StudentQuestionStatisticsResponse;
import com.edutrack.domain.statistics.service.QuestionStatisticsService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuestionStatisticsController {

  private final QuestionStatisticsService questionStatisticsService;

  /**
   * 학생 개인 + 특정 시험 문항별 정답률 조회
   */
  @GetMapping("/student/statistics/exams/{examId}/questions")
  @PreAuthorize("hasRole('STUDENT')")
  public List<StudentQuestionStatisticsResponse> getStudentStats(
      @PathVariable Long examId,
      Principal principal
  ) {

    Long studentId = Long.valueOf(principal.getName());

    return questionStatisticsService.getStudentQuestionStatistics(examId, studentId);
  }

  /**
   * 강사용: 특정 시험 문항별 정답률
   */
  @GetMapping("/statistics/exams/{examId}/questions")
  @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
  public List<QuestionCorrectRateResponse> getExamStats(
      @PathVariable Long examId
  ) {
    return questionStatisticsService.getExamQuestionCorrectRates(examId);
  }

  // 강사용: 강의 전체 문항별 정답률
  @GetMapping("/statistics/lectures/{lectureId}/questions")
  @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
  public List<QuestionCorrectRateResponse> getLectureStats(
      @PathVariable Long lectureId) {

    return questionStatisticsService.getLectureQuestionCorrectRates(lectureId);
  }

}
