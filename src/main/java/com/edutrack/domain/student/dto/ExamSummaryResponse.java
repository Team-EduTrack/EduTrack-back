package com.edutrack.domain.student.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 시험 목록 응답 DTO
 * - JPQL DTO Projection용 클래스
 */
@Getter
@Builder
public class ExamSummaryResponse {
    
    private final Long examId;
    private final String lectureTitle;
    private final String title;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer earnedScore;
    private final String status;

    /**
     * JPQL new 연산자용 생성자
     */
    public ExamSummaryResponse(Long examId, String lectureTitle, String title,
                               LocalDateTime startDate, LocalDateTime endDate,
                               Integer earnedScore, String status) {
        this.examId = examId;
        this.lectureTitle = lectureTitle;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.earnedScore = earnedScore;
        this.status = status;
    }
}
