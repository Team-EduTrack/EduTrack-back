package com.edutrack.domain.exam.dto;

import com.edutrack.domain.exam.entity.StudentExamStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시험 응시 시작 응답 DTO
 * - 시험 정보와 문제 목록 (정답 제외)
 */
@Getter
@Builder
public class ExamStartResponse {
    
    // 기본 정보
    private final Long examId;
    private final Long studentId;
    private final String title;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer durationMinute;
    
    // 응시 상태 정보
    private final LocalDateTime startedAt;
    private final LocalDateTime examStartedAt;  // startedAt과 동일 (하위 호환)
    private final LocalDateTime personalDeadline;
    private final StudentExamStatus status;
    
    // 문제 목록 (B파트에서 사용)
    private final List<QuestionForStudent> questions;

    /**
     * 학생용 문제 정보 (정답 번호 제외)
     */
    @Getter
    @Builder
    public static class QuestionForStudent {
        private final Long questionId;
        private final String content;
        private final Integer score;
        private final String difficulty;
        private final List<ChoiceForStudent> choices;
    }

    /**
     * 학생용 보기 정보
     */
    @Getter
    @Builder
    public static class ChoiceForStudent {
        private final Long choiceId;
        private final String content;
        private final Integer choiceNumber;
    }
}
