package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionIdResponse {
    private final Long questionId;
}
