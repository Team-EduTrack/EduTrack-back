package com.edutrack.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 강사용 문항별 정답률 DTO
 */

@Getter
@AllArgsConstructor
public class QuestionCorrectRateResponse {

  private Long questionId;
  private String questionContent;

  private Long totalCount;
  private Long correctCount;
  private Double correctRate;

}
