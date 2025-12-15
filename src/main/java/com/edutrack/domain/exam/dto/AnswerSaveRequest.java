package com.edutrack.domain.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 답안 저장 요청 DTO
 * - 여러 문제의 답안을 한 번에 저장
 */
@Getter
@Setter
public class AnswerSaveRequest {

    @NotEmpty(message = "답안 목록은 비어있을 수 없습니다.")
    @Valid
    private List<AnswerSubmitRequest> answers;
}




