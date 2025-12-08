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

}
