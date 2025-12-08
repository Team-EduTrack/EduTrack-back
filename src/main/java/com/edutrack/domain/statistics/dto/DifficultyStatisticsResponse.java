package com.edutrack.domain.statistics.dto;


import com.edutrack.domain.exam.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class DifficultyStatisticsResponse {

  private final Difficulty difficulty;
  private final Long totalQuestions;
  private final Long correctQuestions;

  public DifficultyStatisticsResponse(Difficulty difficulty, Long totalQuestions, Long correctQuestions) {
    this.difficulty = difficulty;
    this.totalQuestions = totalQuestions;
    this.correctQuestions = correctQuestions;
  }

}
