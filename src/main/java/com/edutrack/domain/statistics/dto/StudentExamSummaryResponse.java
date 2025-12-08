package com.edutrack.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StudentExamSummaryResponse {
    private final Long examId;
    private final String examTitle;
    private final String lectureName;
    private final Integer totalScore;
    private final Integer earnedScore;
    private final LocalDateTime submittedAt;

    public StudentExamSummaryResponse(Long examId, String examTitle, String lectureName, Integer totalScore, Integer earnedScore) {
        this.examId = examId;
        this.examTitle = examTitle;
        this.lectureName = lectureName;
        this.totalScore = totalScore;
        this.earnedScore = earnedScore;
        this.submittedAt = LocalDateTime.now();
    }
}
