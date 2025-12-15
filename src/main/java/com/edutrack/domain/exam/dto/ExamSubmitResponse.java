package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 시험 제출 응답 DTO
 */
@Getter
@Builder
public class ExamSubmitResponse {
    
    private final Long examId;
    private final Long studentId;
    private final LocalDateTime submittedAt;
    private final String message;
}





