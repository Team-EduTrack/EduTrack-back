package com.edutrack.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 학생 개인 + 전체 정답률 DTO
 */

@Getter
@AllArgsConstructor
public class StudentQuestionStatisticsResponse {

  // 문항 정보
  private Long questionId;
  private String questionContent;
  private Integer correctAnswer;

  // 학생 개인 답안 정보
  private Integer submittedAnswer;
  private boolean isCorrect;
  private Integer earnedScore;
  private Integer maxScore;

  // 전체 통계 정보
  private Long totalCount;      // 전체 응시자 수
  private Long correctCount;    // 정답자 수
  private Double correctRate;   // 정답률(%)

}
