package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamSubmitResponse {
    private Long examId;
    private Long studentId;
    private Integer totalScore;
    private Integer correctCount;
    private Integer wrongCount;
}
