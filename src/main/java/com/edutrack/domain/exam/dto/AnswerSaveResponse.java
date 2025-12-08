package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 답안 저장 응답 DTO
 */
@Getter
@Builder
public class AnswerSaveResponse {
    
    private final Long examId;
    private final Long studentId;
    private final int savedCount;
    private final LocalDateTime savedAt;
}

