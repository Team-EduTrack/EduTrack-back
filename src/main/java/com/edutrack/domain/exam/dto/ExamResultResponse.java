package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시험 결과 응답 DTO
 * - 채점 완료 후 학생에게 보여주는 결과
 */
@Getter
@Builder
public class ExamResultResponse {
    
    private final Long examId;
    private final String examTitle;
    private final Long studentId;
    private final String studentName;
    private final String status;
    private final Integer totalScore;
    private final Integer earnedScore;
    private final Integer correctCount;
    private final Integer totalQuestionCount;
    private final LocalDateTime startedAt;
    private final LocalDateTime submittedAt;
    private final List<QuestionResult> questionResults;

    /**
     * 개별 문제 결과
     */
    @Getter
    @Builder
    public static class QuestionResult {
        private final Long questionId;
        private final String content;
        private final Integer score;
        private final Integer submittedAnswer;
        private final Integer correctAnswer;
        private final boolean isCorrect;
        private final Integer earnedScore;
    }
}




