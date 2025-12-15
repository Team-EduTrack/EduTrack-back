package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 시험 기록 응답 DTO
 * - 학생의 시험 응시 기록 목록용
 */
@Getter
@Builder
public class ExamRecordResponse {
    
    private final Long examId;
    private final String examTitle;
    private final String lectureName;
    private final String status;
    private final Integer totalScore;
    private final Integer earnedScore;
    private final LocalDateTime startedAt;
    private final LocalDateTime submittedAt;
    private final LocalDateTime examStartDate;
    private final LocalDateTime examEndDate;
}




