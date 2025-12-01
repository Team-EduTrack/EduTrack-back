package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamCreationResponse {
    private final Long examId;
}
