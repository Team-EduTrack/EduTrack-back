package com.edutrack.domain.exam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 답안 제출 요청 DTO
 * - 개별 문제에 대한 답안
 */
@Getter
@Setter
public class AnswerSubmitRequest {

    @NotNull(message = "문제 ID는 필수입니다.")
    private Long questionId;

    @NotNull(message = "선택한 답안 번호는 필수입니다.")
    @Min(value = 1, message = "답안 번호는 1 이상이어야 합니다.")
    private Integer selectedAnswerNumber;
}





